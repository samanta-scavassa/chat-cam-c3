package com.c3.chatcamc3;

import com.c3.chatcamc3.exceptions.ChatRoomException;
import com.github.sarxos.webcam.Webcam;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@CrossOrigin
@RestController
@RequestMapping("api/v1/chatcam")
public class ChatCamController {

    public static final Webcam webcam = Webcam.getDefault();
    public static byte[] imageByte;
    public static final Object lock = new Object();

    @PostConstruct
    public void init() {
        if (webcam != null) {
            webcam.setViewSize(new Dimension(640, 480));
            webcam.open(true);
        }
    }

    @GetMapping("/webcam")
    public void captureImage(HttpServletResponse response, ModelMap model, HttpServletRequest request) throws IOException {

        synchronized (ChatCamController.lock) {
            imageByte = null;
            try {

                int i = 0;
                do {
                    if (webcam.getLock().isLocked()) {
                        System.out.println("Waiting for lock to be released " + i);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            response.setContentType("image/png");
                            response.setContentLength(0);
                            response.getOutputStream().write(imageByte);
                            return;
                        }
                    } else {
                        break;
                    }
                } while (i++ < 2);

                webcam.setViewSize(new Dimension(640, 480));
                webcam.open();

                BufferedImage image = webcam.getImage();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                webcam.close();
                ImageIO.write(image, "png", baos);
                baos.flush();
                imageByte = baos.toByteArray();
                baos.close();

                response.setContentType("image/png");
                response.setContentLength(imageByte.length);
                response.getOutputStream().write(imageByte);

            } catch (IOException e) {
                throw new ChatRoomException("Não foi possǘel estabelecer conexão", e.getCause());
            } catch (Exception e) {
                throw new ChatRoomException("Não foi possǘel estabelecer conexão", e.getCause());
            }
        }
    }
}
