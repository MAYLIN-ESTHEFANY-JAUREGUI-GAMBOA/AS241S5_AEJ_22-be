package pe.edu.vallegrande.SistemaIAs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.PartHttpMessageWriter;
import org.springframework.boot.web.codec.CodecCustomizer;

@Configuration
public class MultipartConfig {

    @Bean
    public CodecCustomizer multipartCodecCustomizer() {
        return codecConfigurer -> {
            codecConfigurer.customCodecs().register(new PartHttpMessageWriter());
            codecConfigurer.customCodecs().register(new MultipartHttpMessageReader(new DefaultPartHttpMessageReader()));
        };
    }
}
