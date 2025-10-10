package com.next.move.services;

import com.next.move.enums.NotifType;
import com.next.move.models.Goals;
import com.twilio.Twilio;
import com.twilio.converter.Promoter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.math.BigDecimal;

@Service
public class TwilioService {
    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;
    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;
    @Value("${twilio.phone.number.sms}")
    private String TWILIO_PHONE_NUMBER_SMS;
    @Value("${twilio.phone.number.whatsapp}")
    private String TWILIO_PHONE_NUMBER_WHATSAPP;

    public void sendText(String phoneNumber, String txtMessage, Goals goal) {
        try {
            System.out.println("Sending the text...");
            String senderNumber = TWILIO_PHONE_NUMBER_SMS;
            String prefix = "";
            if (goal.getWhatsappNotif()) {
                prefix = "whatsapp:";
                senderNumber = TWILIO_PHONE_NUMBER_WHATSAPP;
            }
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber(prefix + phoneNumber),
                            new com.twilio.type.PhoneNumber(prefix + senderNumber),
                            txtMessage)
                    .create();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
