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
        String confirmationLink = "http://localhost:4200/api/confirm?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Confirmez votre participation — Véra");
            helper.setText(
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2 style='color: #2a6262;'>Véra — Confirmation de participation</h2>" +
                            "<p>Bonjour,</p>" +
                            "<p>Merci de vous être inscrit à un événement sur Véra.</p>" +
                            "<p>Cliquez sur le bouton ci-dessous pour confirmer votre participation:</p>" +
                            "<a href='" + confirmationLink + "' " +
                            "style='display:inline-block; padding:12px 24px; background:#2a6262; color:white; " +
                            "text-decoration:none; border-radius:8px; font-weight:bold;'>Confirmer ma participation</a>" +
                            "<p style='margin-top:20px; color:#888; font-size:12px;'>Ce lien est valable 24 heures.</p>" +
                            "<p>L'équipe Véra</p>" +
                            "</div>",
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = "http://localhost:4200/api/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            helper.setSubject("Réinitialisation de votre mot de passe — Véra");

            helper.setText(
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2 style='color: #2a6262;'>Véra — Réinitialisation du mot de passe</h2>" +
                            "<p>Bonjour,</p>" +
                            "<p>Vous avez demandé la réinitialisation de votre mot de passe.</p>" +
                            "<p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe:</p>" +
                            "<a href='" + resetLink + "' " +
                            "style='display:inline-block; padding:12px 24px; background:#2a6262; color:white; " +
                            "text-decoration:none; border-radius:8px; font-weight:bold;'>Réinitialiser mon mot de passe</a>" +
                            "<p style='margin-top:20px; color:#888; font-size:12px;'>Ce lien est valable 30 minutes.</p>" +
                            "<p style='color:#888; font-size:12px;'>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>" +
                            "<p>L'équipe Véra</p>" +
                            "</div>",
                    true
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
