package com.example.backend1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String PRIMARY   = "#A53A6B";
    private static final String SECONDARY = "#A53A6B";
    private static final String TEAL      = "#2a6262";

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String header() {
        return "<div style='font-family: Georgia, serif; max-width: 600px; " +
                "margin: 0 auto; background: #f8f5f0; padding: 40px 20px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<div style='width: 60px; height: 2px; background:" + PRIMARY + "; margin: 0 auto 20px;'></div>" +
                "<h1 style='color:" + PRIMARY + "; font-size: 32px; margin: 0; letter-spacing: 2px;'>Invitini</h1>" +
                "<div style='width: 60px; height: 2px; background:" + PRIMARY + "; margin: 20px auto 0;'></div>" +
                "</div>";
    }

    private String footer() {
        return "<div style='text-align: center; margin-top: 30px; color: #999; font-size: 12px;'>" +
                "<p>© 2026 Invitini - Plateforme d'événements culturels</p>" +
                "</div></div>";
    }

    private String cardOpen() {
        return "<div style='background: white; border-radius: 12px; padding: 40px; " +
                "box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 2px solid #e8e4dc; position: relative;'>" +
                "<div style='position:absolute;top:15px;left:15px;width:40px;height:40px;" +
                "border-top:2px solid rgba(165,58,107,0.3);border-left:2px solid rgba(165,58,107,0.3);border-radius:8px 0 0 0;'></div>" +
                "<div style='position:absolute;bottom:15px;right:15px;width:40px;height:40px;" +
                "border-bottom:2px solid rgba(165,58,107,0.3);border-right:2px solid rgba(165,58,107,0.3);border-radius:0 0 8px 0;'></div>" +
                "<div style='text-align: center; padding: 20px;'>";
    }

    private String cardClose() {
        return "</div></div>";
    }

    private String divider() {
        return "<div style='width:80%;height:1px;background:linear-gradient(to right," +
                "transparent,rgba(165,58,107,0.3),transparent);margin:25px auto;'></div>";
    }

    private String button(String link, String label) {
        return "<div style='margin: 35px 0;'>" +
                "<a href='" + link + "' style='display:inline-block;background:" + PRIMARY + ";" +
                "color:white;padding:16px 40px;text-decoration:none;border-radius:30px;" +
                "font-weight:600;font-size:15px;letter-spacing:1px;" +
                "box-shadow:0 4px 15px rgba(165,58,107,0.3);'>" + label + "</a>" +
                "</div>";
    }

    private String teamSignature(String lang) {
        return "<p style='color:" + PRIMARY + ";font-size:14px;font-weight:600;margin:25px 0 0 0;'>" +
                (lang.equals("en") ? "The Invitini Team" : "L'équipe Invitini") + "</p>";
    }

    private boolean isFr(String lang) {
        return !"en".equalsIgnoreCase(lang);
    }

    // ─── 1. Confirmation participation ─────────────────────────────────────

    public void sendConfirmationEmail(String toEmail, String token, String lang) {
        String link = "http://localhost:4200/confirm?token=" + token;
        boolean fr = isFr(lang);

        String subject = fr
                ? "Confirmez votre participation — Invitini"
                : "Confirm your participation — Invitini";

        String body = header() + cardOpen() +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Invitation à un événement" : "Event invitation") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;line-height:1.4;'>" +
                (fr ? "Vous êtes invité(e) à participer" : "You are invited to participate") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Félicitations ! Vous vous êtes inscrit à un événement sur Invitini."
                        : "Congratulations! You have registered for an event on Invitini.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Pour confirmer votre présence, veuillez cliquer sur le bouton ci-dessous :"
                        : "To confirm your attendance, please click the button below:") + "</p>" +
                button(link, fr ? "Confirmer ma participation" : "Confirm my participation") +
                divider() +
                "<p style='color:#999;font-size:13px;margin:20px 0;'>" +
                (fr ? "Cette invitation est valable 24 heures.<br>Nous avons hâte de vous voir !"
                        : "This link is valid for 24 hours.<br>We look forward to seeing you!") + "</p>" +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    // Keep old signature for backward compatibility
    public void sendConfirmationEmail(String toEmail, String token) {
        sendConfirmationEmail(toEmail, token, "fr");
    }

    // ─── 2. Password reset ──────────────────────────────────────────────────

    public void sendPasswordResetEmail(String toEmail, String token, String lang) {
        String link = "http://localhost:4200/reset-password?token=" + token;
        boolean fr = isFr(lang);

        String subject = fr
                ? "Réinitialisation de votre mot de passe — Invitini"
                : "Reset your password — Invitini";

        String body = header() + cardOpen() +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Sécurité du compte" : "Account security") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;line-height:1.4;'>" +
                (fr ? "Réinitialisation du mot de passe" : "Password reset") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Vous avez demandé la réinitialisation de votre mot de passe."
                        : "You requested a password reset.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :"
                        : "Click the button below to create a new password:") + "</p>" +
                button(link, fr ? "Réinitialiser mon mot de passe" : "Reset my password") +
                divider() +
                "<p style='color:#999;font-size:13px;margin:20px 0;'>" +
                (fr ? "Ce lien est valable 30 minutes.<br>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email."
                        : "This link is valid for 30 minutes.<br>If you did not request this, please ignore this email.") + "</p>" +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        sendPasswordResetEmail(toEmail, token, "fr");
    }

    // ─── 3. Account verification ────────────────────────────────────────────

    public void sendVerificationEmail(String toEmail, String token, String userType, String lang) {
        String link = "http://localhost:4200/verify-account?token=" + token;
        boolean fr = isFr(lang);

        String subject = fr
                ? "Vérifiez votre compte — Invitini"
                : "Verify your account — Invitini";

        String body = header() + cardOpen() +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>Invitini</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;line-height:1.4;'>" +
                (fr ? "Vous êtes cordialement invité(e) à rejoindre Invitini"
                        : "You are cordially invited to join Invitini") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Bienvenue sur Invitini !<br>Vous vous êtes inscrit en tant que <strong style='color:" + PRIMARY + ";'>" + userType + "</strong>."
                        : "Welcome to Invitini!<br>You signed up as <strong style='color:" + PRIMARY + ";'>" + userType + "</strong>.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Pour activer votre compte, veuillez confirmer votre invitation :"
                        : "To activate your account, please confirm your invitation:") + "</p>" +
                button(link, fr ? "Confirmer mon invitation" : "Confirm my invitation") +
                divider() +
                "<p style='color:#999;font-size:13px;margin:20px 0;'>" +
                (fr ? "Cette invitation est valable 24 heures.<br>Si vous n'avez pas créé de compte, ignorez cet email."
                        : "This link is valid for 24 hours.<br>If you did not create an account, ignore this email.") + "</p>" +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    // Keep old signature
    public void sendVerificationEmail(String toEmail, String token, String userType) {
        sendVerificationEmail(toEmail, token, userType, "fr");
    }

    // ─── 4. Organiser verified ──────────────────────────────────────────────

    public void sendOrganisateurVerifiedEmail(String toEmail, String lang) {
        boolean fr = isFr(lang);

        String subject = fr
                ? "Votre compte organisateur est vérifié — Invitini"
                : "Your organizer account is verified — Invitini";

        String body = header() + cardOpen() +
                "<div style='width:70px;height:70px;background:#fce4ef;border-radius:50%;" +
                "display:flex;align-items:center;justify-content:center;margin:0 auto 24px;'>" +
                "<span style='font-size:32px;color:" + PRIMARY + ";'>✓</span></div>" +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Compte vérifié" : "Account verified") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;'>" +
                (fr ? "Félicitations !" : "Congratulations!") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre compte organisateur a été <strong style='color:" + PRIMARY + ";'>vérifié par notre équipe</strong>."
                        : "Your organizer account has been <strong style='color:" + PRIMARY + ";'>verified by our team</strong>.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Vous pouvez désormais créer et publier des événements sur Invitini."
                        : "You can now create and publish events on Invitini.") + "</p>" +
                button("http://localhost:4200/home",
                        fr ? "Créer mon premier événement" : "Create my first event") +
                divider() +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    public void sendOrganisateurVerifiedEmail(String toEmail) {
        sendOrganisateurVerifiedEmail(toEmail, "fr");
    }

    // ─── 5. Event approved ──────────────────────────────────────────────────

    public void sendEventApprovedEmail(String toEmail, String eventTitle, String lang) {
        boolean fr = isFr(lang);

        String subject = fr
                ? "Votre événement est approuvé — Invitini"
                : "Your event is approved — Invitini";

        String body = header() + cardOpen() +
                "<div style='width:70px;height:70px;background:#fce4ef;border-radius:50%;" +
                "display:flex;align-items:center;justify-content:center;margin:0 auto 24px;'>" +
                "<span style='font-size:32px;'>📅</span></div>" +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Événement approuvé" : "Event approved") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;'>" +
                (fr ? "Votre événement est en ligne !" : "Your event is live!") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre événement <strong style='color:" + PRIMARY + ";'>\"" + eventTitle + "\"</strong> a été approuvé."
                        : "Your event <strong style='color:" + PRIMARY + ";'>\"" + eventTitle + "\"</strong> has been approved.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Il est désormais visible et les inscriptions sont ouvertes."
                        : "It is now visible and registrations are open.") + "</p>" +
                button("http://localhost:4200/events",
                        fr ? "Voir mon événement" : "View my event") +
                divider() +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    public void sendEventApprovedEmail(String toEmail, String eventTitle) {
        sendEventApprovedEmail(toEmail, eventTitle, "fr");
    }

    // ─── 6. Contact form ────────────────────────────────────────────────────

    public void sendContactEmail(String fromName, String fromEmail, String sujet, String message) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");
            helper.setTo("invitini.events@gmail.com");
            helper.setSubject("[Invitini Contact] " + sujet + " — " + fromName);
            helper.setText(buildContactContent(fromName, fromEmail, sujet, message), true);
            helper.setReplyTo(fromEmail);
            mailSender.send(mail);
        } catch (Exception e) {
            System.err.println("Failed to send contact email: " + e.getMessage());
            throw new RuntimeException("Failed to send contact email", e);
        }
    }

    private String buildContactContent(String fromName, String fromEmail,
                                       String sujet, String message) {
        return header() +
                "<div style='background:white;border-radius:12px;padding:36px;border:2px solid #e8e4dc;'>" +
                "<p style='font-size:13px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;font-weight:600;margin:0 0 20px;'>Nouveau message de contact</p>" +
                "<table style='width:100%;border-collapse:collapse;margin-bottom:24px;'>" +
                "<tr><td style='padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;width:120px;'>Nom</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px;color:#1a1a1a;font-weight:600;'>" + fromName + "</td></tr>" +
                "<tr><td style='padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;'>Email</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px;color:" + PRIMARY + ";'>" + fromEmail + "</td></tr>" +
                "<tr><td style='padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;'>Sujet</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px;color:#1a1a1a;'>" + sujet + "</td></tr>" +
                "</table>" +
                "<p style='font-size:13px;color:#888;margin:0 0 12px;text-transform:uppercase;letter-spacing:1px;'>Message</p>" +
                "<div style='background:#fdf5f9;border-left:3px solid " + PRIMARY + ";border-radius:4px;padding:16px 20px;'>" +
                "<p style='font-size:15px;color:#333;line-height:1.8;margin:0;white-space:pre-wrap;'>" + message + "</p>" +
                "</div>" +
                "<p style='font-size:13px;color:#aaa;margin:24px 0 0;text-align:center;'>" +
                "Répondez directement à cet email pour contacter <strong>" + fromName + "</strong> — " + fromEmail + "</p>" +
                "</div>" + footer();
    }

    // ─── 7. Account deactivation request (organizer) ────────────────────────

    public void sendDeactivationRequestEmail(String toAdminEmail, String orgName,
                                             String orgEmail, String lang) {
        boolean fr = isFr(lang);
        String subject = fr
                ? "Demande de désactivation de compte — " + orgName
                : "Account deactivation request — " + orgName;

        String body = header() +
                "<div style='background:white;border-radius:12px;padding:36px;border:2px solid #e8e4dc;'>" +
                "<p style='font-size:13px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;font-weight:600;margin:0 0 20px;'>" +
                (fr ? "Demande de désactivation" : "Deactivation request") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:22px;margin:0 0 20px;'>" +
                (fr ? "Un organisateur souhaite désactiver son compte"
                        : "An organizer wants to deactivate their account") + "</h2>" +
                divider() +
                "<table style='width:100%;border-collapse:collapse;margin-bottom:24px;'>" +
                "<tr><td style='padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;width:120px;'>" +
                (fr ? "Nom" : "Name") + "</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:14px;color:#1a1a1a;font-weight:600;'>" + orgName + "</td></tr>" +
                "<tr><td style='padding:10px 0;color:#888;font-size:13px;'>Email</td>" +
                "<td style='padding:10px 0;font-size:14px;color:" + PRIMARY + ";'>" + orgEmail + "</td></tr>" +
                "</table>" +
                "<p style='color:#666;font-size:14px;line-height:1.6;'>" +
                (fr ? "Veuillez vérifier les événements en cours avant d'approuver cette demande dans le tableau de bord admin."
                        : "Please review any upcoming events before approving this request in the admin dashboard.") + "</p>" +
                button("http://localhost:4200/admin/dashboard",
                        fr ? "Gérer la demande" : "Manage request") +
                teamSignature(lang) + "</div>" + footer();

        send(toAdminEmail, subject, body);
    }

    // ─── 8. Account deactivation confirmed (user/organizer) ─────────────────

    public void sendDeactivationConfirmedEmail(String toEmail, String lang) {
        boolean fr = isFr(lang);
        String subject = fr
                ? "Votre compte a été désactivé — Invitini"
                : "Your account has been deactivated — Invitini";

        String body = header() + cardOpen() +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Compte désactivé" : "Account deactivated") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;'>" +
                (fr ? "Votre compte a été désactivé" : "Your account has been deactivated") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre demande de désactivation a été traitée. Votre compte est maintenant inactif."
                        : "Your deactivation request has been processed. Your account is now inactive.") + "</p>" +
                "<p style='color:#666;font-size:14px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Si vous souhaitez réactiver votre compte, contactez-nous à support@invitini.tn."
                        : "To reactivate your account, contact us at support@invitini.tn.") + "</p>" +
                divider() +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    // ─── Shared send helper ──────────────────────────────────────────────────

    private void send(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("Email sent to: " + toEmail + " | Subject: " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}