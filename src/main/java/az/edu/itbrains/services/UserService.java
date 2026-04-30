package az.edu.itbrains.services;


import az.edu.itbrains.DTOs.request.*;
import az.edu.itbrains.DTOs.response.AuthResponseDTO;
import az.edu.itbrains.models.User;

public interface UserService {

    User getCurrentUser();

    AuthResponseDTO registerUser(RegisterRequestDTO request);

    // Tokensiz təsdiqləmə.
    AuthResponseDTO verifyUser(VerifyRequestDTO request);

    // Tokensiz təkrar göndərmə.
    void resendOtp(ResendRequestDTO request);

    AuthResponseDTO loginUser(LoginRequestDTO request);

    AuthResponseDTO refreshToken(String refreshToken);


    AuthResponseDTO getUserProfile(String email);

    void forgotPassword(ForgotPasswordRequestDTO request);
    AuthResponseDTO resetPassword(ResetPasswordRequestDTO request);

    void logout(String authHeader, String refreshToken);

    void resendForgotPasswordOtp(ResendRequestDTO request); // Yeni əlavə edildi
}
