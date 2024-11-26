package com.ltcuong.identity_service.service;


import com.ltcuong.identity_service.dto.request.AuthenticationRequest;
import com.ltcuong.identity_service.dto.request.IntrospectRequest;
import com.ltcuong.identity_service.dto.request.LogoutRequest;
import com.ltcuong.identity_service.dto.request.RefreshRequest;
import com.ltcuong.identity_service.dto.response.AuthenticationResponse;
import com.ltcuong.identity_service.dto.response.IntrospectResponse;
import com.ltcuong.identity_service.entity.InvalidatedToken;
import com.ltcuong.identity_service.exception.AppException;
import com.ltcuong.identity_service.exception.ErrorCode;
import com.ltcuong.identity_service.entity.User;
import com.ltcuong.identity_service.repository.InvalidatedTokenRepository;
import com.ltcuong.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;

    InvalidatedTokenRepository invalidatedTokenRepository;

    // key cho sign token
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // hàm xác thực username bằng token
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // kiểm tra xem user name đã tồn tại hay chưa
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // matches(): so sánh 2 chuỗi đã được mã hóa
        // so sánh password của request vs password của entity.
        var authenticate = passwordEncoder.matches(request.getPassword(), user.getPassword());

        // kiểm tra người dùng nhập đúng mật khẩu hay chưa
        if (!authenticate)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .authenticated(authenticate)
                .token(token)
                .build();

    }

    // function refresh token
    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {

        var signedJWT = verifyToken(request.getToken(),true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED)
        );

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();

    }

    // function xác minh token

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        // lấy token từ request
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token,false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }


    // function tạo token
    public String generateToken(User user) {
        // tạo header cho token
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // tạo claim= data truyền vào payload cho token
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("ltcuong.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        // chuyển claim sang Json rồi truyền vào payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // truyền header, payload để tạo object token
        JWSObject jwsObject = new JWSObject(header, payload);

        // sign(kí) token
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
        var signedToken = verifyToken(request.getToken(),true);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);}
        catch (AppException exception){
            log.info("Token already epiried");
        }

    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {

        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

        // parse token sang object signedJWT
        SignedJWT signedJWT = SignedJWT.parse(token);


        // kiểm tra thời gian của token hết hay chưa
        Date expityTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                    .toInstant().plus(REFRESHABLE_DURATION,ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // kiểm tra token
        var verify = signedJWT.verify(jwsVerifier);

        if (!(verify && expityTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;

    }

    // build scope tu User
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        // kiem tra Role cua User co rong hay khong
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            // khong rong
            // add role user vao stringJoiner
            user.getRoles().forEach(role ->
            {
                stringJoiner.add("ROLE_" + role.getName());

                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }

        return stringJoiner.toString();
    }
}
