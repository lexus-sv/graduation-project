package main.service;

import main.api.auth.response.CaptchaResponse;
import main.model.CaptchaCode;
import main.repository.CaptchaCodeRepository;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

@Service
public class CaptchaService {

    @Autowired
    private CaptchaCodeRepository captchaCodeRepository;

    @Value("${captcha.expirationTime}")
    private int CAPTCHA_EXPIRATION_TIME;
    private static final String DATA_IMAGE = "data:image/png;base64, ";
    @Value("${captcha.length}")
    private int CAPTCHA_LENGTH;
    private final static Color FONT_COLOR = Color.WHITE;
    private final static Color IMAGE_BACKGROUND_COLOR = Color.GRAY;
    @Value("${captcha.font.size}")
    private int FONT_SIZE;
    @Value("${captcha.allowedSymbols}")
    private String ALLOWED_SYMBOLS;

    public CaptchaResponse createCaptcha() throws IOException {
        clearExpiredCaptcha();

        String captcha = generateRandomString(CAPTCHA_LENGTH);
        String secret = generateRandomString(CAPTCHA_LENGTH *2);
        int width = CAPTCHA_LENGTH *18, height = 35;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.OPAQUE);
        Graphics graphics = bufferedImage.createGraphics();
        graphics.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        graphics.setColor(IMAGE_BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(FONT_COLOR);
        graphics.drawString(captcha, 20, 25);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        outputStream.flush();
        String encoded = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setSecretCode(secret);
        //Записываю expiration date в time, чтобы легче было проверять и удалять
        captchaCode.setTime(new Date(new Date().getTime() + CAPTCHA_EXPIRATION_TIME));
        captchaCode.setCode(captcha);
        captchaCodeRepository.save(captchaCode);

        LogManager.getLogger().log(Level.INFO, "Captcha generated " + captchaCode);
        return new CaptchaResponse(secret, DATA_IMAGE+encoded);
    }


    public String generateRandomString(int size){
        StringBuilder captchaBuffer = new StringBuilder();
        Random random = new Random();
        while (captchaBuffer.length()<size){
            int index = (int) (random.nextFloat()*ALLOWED_SYMBOLS.length());
            captchaBuffer.append(ALLOWED_SYMBOLS.substring(index, index+1));
        }
        return captchaBuffer.toString();
    }

    private void clearExpiredCaptcha(){
        captchaCodeRepository.removeAllByTimeBefore(new Date());
    }

    public boolean isValidCaptcha(String code, String secret){
        CaptchaCode captcha = captchaCodeRepository.findFirstBySecretCode(secret).orElse(null);
        return captcha != null && captcha.getCode().equals(code);
    }
}
