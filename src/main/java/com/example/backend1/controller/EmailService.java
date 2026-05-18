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

    public void sendConfirmationEmail(String toEmail, String token) {
        sendConfirmationEmail(toEmail, token, "fr");
    }


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

    public void sendVerificationEmail(String toEmail, String token, String userType) {
        sendVerificationEmail(toEmail, token, userType, "fr");
    }


    public void sendOrganisateurVerifiedEmail(String toEmail, String lang) {
        boolean fr = isFr(lang);

        String subject = fr
                ? "Votre compte organisateur est vérifié — Invitini"
                : "Your organizer account is verified — Invitini";

        String body = header() + cardOpen() +
                "<div style='width:70px;height:70px;background:#fce4ef;border-radius:50%;" +
                "text-align:center;line-height:70px;margin:0 auto 24px;" +
                "font-size:32px;color:" + PRIMARY + ";'>✓</div>" +
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


    public void sendEventApprovedEmail(String toEmail, String eventTitle, String lang) {
        boolean fr = isFr(lang);

        String subject = fr
                ? "Votre événement est approuvé — Invitini"
                : "Your event is approved — Invitini";

        String body = header() + cardOpen() +
        "<div style='width:70px;height:70px;background:#fce4ef;border-radius:50%;margin:0 auto 24px;'></div>" +
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
    public void sendEventRejectedEmail(String toEmail, String eventTitle, String lang) {
        boolean fr = isFr(lang);

        String subject = fr
                ? "Votre événement a été refusé — Invitini"
                : "Your event has been rejected — Invitini";

        String body = header() + cardOpen() +
                "<div style='width:70px;height:70px;background:#fce4ef;border-radius:50%;margin:0 auto 24px;" +
                "text-align:center;line-height:70px;font-size:32px;color:" + PRIMARY + ";'>✕</div>" +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Événement refusé" : "Event rejected") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:24px;margin:0 0 20px;'>" +
                (fr ? "Votre événement n'a pas été approuvé" : "Your event was not approved") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:16px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre événement <strong style='color:" + PRIMARY + ";'>\"" + eventTitle + "\"</strong> a été examiné et refusé par notre équipe."
                        : "Your event <strong style='color:" + PRIMARY + ";'>\"" + eventTitle + "\"</strong> was reviewed and rejected by our team.") + "</p>" +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Pour toute question, contactez-nous à <strong>support@invitini.tn</strong>."
                        : "For any questions, contact us at <strong>support@invitini.tn</strong>.") + "</p>" +
                button("http://localhost:4200/home",
                        fr ? "Retour à la plateforme" : "Back to platform") +
                divider() +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    public void sendEventRejectedEmail(String toEmail, String eventTitle) {
        sendEventRejectedEmail(toEmail, eventTitle, "fr");
    }

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
                (fr ? "Votre compte est désactivé" : "Your account is deactivated") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre compte a été désactivé avec succès. Vous ne pouvez plus vous connecter à Invitini."
                        : "Your account has been successfully deactivated. You can no longer sign in to Invitini.") + "</p>" +
                "<p style='color:#666;font-size:14px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Si vous souhaitez réactiver votre compte à l'avenir, contactez-nous à <strong>support@invitini.tn</strong> en précisant votre adresse email."
                        : "If you wish to reactivate your account in the future, contact us at <strong>support@invitini.tn</strong> with your email address.") + "</p>" +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }

    public void sendDeactivationRejectedEmail(String toEmail, String lang) {
        boolean fr = isFr(lang);
        String subject = fr
                ? "Demande de désactivation refusée — Invitini"
                : "Account deactivation request rejected — Invitini";

        String body = header() + cardOpen() +
                "<p style='font-size:14px;color:" + PRIMARY + ";text-transform:uppercase;" +
                "letter-spacing:2px;margin:0 0 20px;font-weight:600;'>" +
                (fr ? "Demande refusée" : "Request rejected") + "</p>" +
                "<h2 style='color:#1a1a1a;font-size:22px;margin:0 0 16px;'>" +
                (fr ? "Votre demande a été refusée" : "Your request was rejected") + "</h2>" +
                divider() +
                "<p style='color:#666;font-size:15px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Votre demande de désactivation a été examinée et refusée par notre équipe. Votre compte reste actif."
                        : "Your deactivation request was reviewed and rejected. Your account remains active.") + "</p>" +
                "<p style='color:#666;font-size:14px;line-height:1.6;margin:20px 0;'>" +
                (fr ? "Si vous avez des questions, contactez-nous à support@invitini.tn."
                        : "If you have questions, reach us at support@invitini.tn.") + "</p>" +
                button("http://localhost:4200/home", fr ? "Retour à la plateforme" : "Back to platform") +
                teamSignature(lang) + cardClose() + footer();

        send(toEmail, subject, body);
    }


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

    public void sendQrTicketEmail(String to, String token,
                                  String eventTitle, String eventDate,
                                  String eventLocation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(" Votre billet — " + eventTitle);

            // QR code image URL — points to your scan endpoint
            // The QR encodes the token so the organizer's scanner reads it
            String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + token;

            String html = """
            <div style="font-family: Georgia, serif; max-width: 520px; margin: auto; padding: 32px; background: #ffffff; border-radius: 12px; border: 1px solid #f0dde7;">
              <h2 style="color: #A53A6B; text-align: center; margin-bottom: 4px;">Invitini</h2>
              <p style="text-align: center; color: #888; font-size: 13px; margin-bottom: 28px;">Votre participation est confirmée !</p>

              <div style="background: #fdf5f8; border-radius: 10px; padding: 20px; margin-bottom: 24px; text-align: center;">
                <h3 style="color: #1a1a1a; margin: 0 0 8px;">""" + eventTitle + """
                </h3>
                <p style="color: #666; font-size: 14px; margin: 4px 0;">
                  """ + eventDate + """
                </p>
                <p style="color: #666; font-size: 14px; margin: 4px 0;">
                   """ + eventLocation + """
                </p>
              </div>

              <div style="text-align: center; margin-bottom: 24px;">
                <p style="color: #A53A6B; font-weight: 700; font-size: 15px; margin-bottom: 12px;">
                  ️ Votre billet d'entrée
                </p>
                <img src=\"""" + qrImageUrl + """
                     " alt="QR Code" style="width: 180px; height: 180px; border: 3px solid #f0dde7; border-radius: 10px; padding: 8px; background: white;" />
                <p style="color: #999; font-size: 12px; margin-top: 10px;">
                  Présentez ce QR code à l'entrée de l'événement.<br>
                  L'organisateur le scannera pour enregistrer votre présence.
                </p>
              </div>

              <div style="background: #fff3cd; border-radius: 8px; padding: 12px 16px; margin-bottom: 20px;">
                <p style="color: #856404; font-size: 13px; margin: 0;">
                  ! Ne partagez pas ce QR code — il est personnel et unique.
                </p>
              </div>

              <p style="text-align: center; color: #bbb; font-size: 12px;">
                Invitini — Plateforme d'événements culturels et éducatifs en Tunisie
              </p>
            </div>
            """;

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send QR ticket email: " + e.getMessage());
        }
    }
}