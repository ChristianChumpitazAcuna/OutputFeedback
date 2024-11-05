package pe.edu.vallegrande.demoofb.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.vallegrande.demoofb.application.service.CryptoService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/crypto")
public class CryptoController {

    private final CryptoService service;

    public CryptoController(CryptoService service) {
        this.service = service;
    }

    @PostMapping(value = "/encrypt")
    public Mono<String> encryptText(@RequestBody String request) {
        return service.encrypt(request);
    }

    @PostMapping(value = "/decrypt")
    public Mono<String> decryptText(@RequestBody String request) {
        return service.decrypt(request);
    }
}
