package com.example.backend1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendConfirmationEmail (String toEmail,String token){
        String confirmationLink = "http://localhost:4200/confirm?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Confirmez votre participation — Invitini");
            helper.setText(
                    "<div style='font-family: Georgia, serif; max-width: 600px; margin: 0 auto; background: #f8f5f0; padding: 40px 20px;'>" +

                            "<!-- Header -->" +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<div style='width: 60px; height: 2px; background: #2a6262; margin: 0 auto 20px;'></div>" +
                            "<h1 style='color: #2a6262; font-size: 32px; margin: 0; letter-spacing: 2px;'>Invitini</h1>" +
                            "<div style='width: 60px; height: 2px; background: #2a6262; margin: 20px auto 0;'></div>" +
                            "</div>" +

                            "<!-- Invitation Card -->" +
                            "<div style='background: white; border-radius: 12px; padding: 40px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 2px solid #e8e4dc; position: relative;'>" +

                            "<!-- Corner Decorations -->" +
                            "<div style='position: absolute; top: 15px; left: 15px; width: 40px; height: 40px; border-top: 2px solid rgba(42,98,98,0.3); border-left: 2px solid rgba(42,98,98,0.3); border-radius: 8px 0 0 0;'></div>" +
                            "<div style='position: absolute; bottom: 15px; right: 15px; width: 40px; height: 40px; border-bottom: 2px solid rgba(42,98,98,0.3); border-right: 2px solid rgba(42,98,98,0.3); border-radius: 0 0 8px 0;'></div>" +

                            "<!-- Content -->" +
                            "<div style='text-align: center; padding: 20px;'>" +
                            "<p style='font-size: 14px; color: #2a6262; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 20px 0; font-weight: 600;'>Invitation à un événement</p>" +

                            "<h2 style='color: #1a1a1a; font-size: 24px; margin: 0 0 20px 0; line-height: 1.4;'>Vous êtes invité(e) à participer</h2>" +

                            "<div style='width: 80%; height: 1px; background: linear-gradient(to right, transparent, rgba(42,98,98,0.3), transparent); margin: 25px auto;'></div>" +

                            "<p style='color: #666; font-size: 16px; line-height: 1.6; margin: 20px 0;'>" +
                            "Félicitations! Vous vous êtes inscrit à un événement sur Invitini." +
                            "</p>" +

                            "<p style='color: #666; font-size: 15px; line-height: 1.6; margin: 20px 0;'>" +
                            "Pour confirmer votre présence et recevoir tous les détails de l'événement, " +
                            "veuillez cliquer sur le bouton ci-dessous:" +
                            "</p>" +

                            "<!-- Button -->" +
                            "<div style='margin: 35px 0;'>" +
                            "<a href='" + confirmationLink + "' style='display: inline-block; background: #2a6262; color: white; padding: 16px 40px; text-decoration: none; border-radius: 30px; font-weight: 600; font-size: 15px; letter-spacing: 1px; box-shadow: 0 4px 15px rgba(42,98,98,0.3);'>" +
                            "Confirmer ma participation" +
                            "</a>" +
                            "</div>" +

                            "<div style='width: 80%; height: 1px; background: linear-gradient(to right, transparent, rgba(42,98,98,0.3), transparent); margin: 25px auto;'></div>" +

                            "<p style='color: #999; font-size: 13px; margin: 20px 0;'>" +
                            "Cette invitation est valable 24 heures.<br>" +
                            "Nous avons hâte de vous voir à cet événement!" +
                            "</p>" +

                            "<p style='color: #2a6262; font-size: 14px; font-weight: 600; margin: 25px 0 0 0;'>" +
                            "L'équipe Invitini" +
                            "</p>" +
                            "</div>" +
                            "</div>" +

                            "<!-- Footer -->" +
                            "<div style='text-align: center; margin-top: 30px; color: #999; font-size: 12px;'>" +
                            "<p>© 2026 Invitini - Plateforme d'événements culturels</p>" +
                            "</div>" +

                            "</div>",
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            helper.setSubject("Réinitialisation de votre mot de passe — Invitini");

            helper.setText(
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2 style='color: #2a6262;'>Invitini — Réinitialisation du mot de passe</h2>" +
                            "<p>Bonjour,</p>" +
                            "<p>Vous avez demandé la réinitialisation de votre mot de passe.</p>" +
                            "<p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe:</p>" +
                            "<a href='" + resetLink + "' " +
                            "style='display:inline-block; padding:12px 24px; background:#2a6262; color:white; " +
                            "text-decoration:none; border-radius:8px; font-weight:bold;'>Réinitialiser mon mot de passe</a>" +
                            "<p style='margin-top:20px; color:#888; font-size:12px;'>Ce lien est valable 30 minutes.</p>" +
                            "<p style='color:#888; font-size:12px;'>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>" +
                            "<p>L'équipe Invitini</p>" +
                            "</div>",
                    true
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    public void sendVerificationEmail(String toEmail, String token, String userType) {
        String verifyLink = "http://localhost:4200/verify-account?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            helper.setSubject("Vérifiez votre compte — Invitini");

            helper.setText(
                    "<div style='font-family: Georgia, serif; max-width: 600px; margin: 0 auto; background: #f8f5f0; padding: 40px 20px;'>" +

                            "<!-- Decorative Header -->" +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<div style='width: 60px; height: 2px; background: #2a6262; margin: 0 auto 20px;'></div>" +
                            "<h1 style='color: #2a6262; font-size: 32px; margin: 0; letter-spacing: 2px;'>Invitini</h1>" +
                            "<div style='width: 60px; height: 2px; background: #2a6262; margin: 20px auto 0;'></div>" +
                            "</div>" +

                            "<!-- Invitation Card -->" +
                            "<div style='background: white; border-radius: 12px; padding: 40px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 2px solid #e8e4dc; position: relative;'>" +

                            "<!-- Corner Decorations -->" +
                            "<div style='position: absolute; top: 15px; left: 15px; width: 40px; height: 40px; border-top: 2px solid rgba(42,98,98,0.3); border-left: 2px solid rgba(42,98,98,0.3); border-radius: 8px 0 0 0;'></div>" +
                            "<div style='position: absolute; bottom: 15px; right: 15px; width: 40px; height: 40px; border-bottom: 2px solid rgba(42,98,98,0.3); border-right: 2px solid rgba(42,98,98,0.3); border-radius: 0 0 8px 0;'></div>" +

                            "<!-- Content -->" +
                            "<div style='text-align: center; padding: 20px;'>" +
                            "<p style='font-size: 14px; color: #2a6262; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 20px 0; font-weight: 600;'>Invitation</p>" +

                            "<h2 style='color: #1a1a1a; font-size: 24px; margin: 0 0 20px 0; line-height: 1.4;'>Vous êtes cordialement invité(e) à rejoindre Invitini</h2>" +

                            "<div style='width: 80%; height: 1px; background: linear-gradient(to right, transparent, rgba(42,98,98,0.3), transparent); margin: 25px auto;'></div>" +

                            "<p style='color: #666; font-size: 16px; line-height: 1.6; margin: 20px 0;'>" +
                            "Bienvenue sur Invitini!<br>Vous vous êtes inscrit en tant que <strong style='color: #2a6262;'>" + userType + "</strong>." +
                            "</p>" +

                            "<p style='color: #666; font-size: 15px; line-height: 1.6; margin: 20px 0;'>" +
                            "Pour activer votre compte et commencer à découvrir des événements culturels exceptionnels, " +
                            "veuillez confirmer votre invitation en cliquant sur le bouton ci-dessous:" +
                            "</p>" +

                            "<!-- Button -->" +
                            "<div style='margin: 35px 0;'>" +
                            "<a href='" + verifyLink + "' style='display: inline-block; background: #2a6262; color: white; padding: 16px 40px; text-decoration: none; border-radius: 30px; font-weight: 600; font-size: 15px; letter-spacing: 1px; box-shadow: 0 4px 15px rgba(42,98,98,0.3);'>" +
                            "Confirmer mon invitation" +
                            "</a>" +
                            "</div>" +

                            "<div style='width: 80%; height: 1px; background: linear-gradient(to right, transparent, rgba(42,98,98,0.3), transparent); margin: 25px auto;'></div>" +

                            "<p style='color: #999; font-size: 13px; margin: 20px 0;'>" +
                            "Cette invitation est valable 24 heures.<br>" +
                            "Si vous n'avez pas créé de compte, veuillez ignorer cet email." +
                            "</p>" +

                            "<p style='color: #2a6262; font-size: 14px; font-weight: 600; margin: 25px 0 0 0;'>" +
                            "L'équipe Invitini" +
                            "</p>" +
                            "</div>" +
                            "</div>" +

                            "<!-- Footer -->" +
                            "<div style='text-align: center; margin-top: 30px; color: #999; font-size: 12px;'>" +
                            "<p>© 2026 Invitini - Plateforme d'événements culturels</p>" +
                            "</div>" +

                            "</div>",
                    true
            );

            mailSender.send(message);

            System.out.println("Verification email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }


    }





}
