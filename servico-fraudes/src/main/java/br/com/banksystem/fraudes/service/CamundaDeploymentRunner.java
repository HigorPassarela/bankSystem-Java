package br.com.banksystem.fraudes.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CamundaDeploymentRunner implements CommandLineRunner {

    private final CamundaDeploymentService camundaDeploymentService;

    public CamundaDeploymentRunner(CamundaDeploymentService camundaDeploymentService) {
        this.camundaDeploymentService = camundaDeploymentService;
    }

    @Override
    public void run(String... args) {
        camundaDeploymentService.deployarProcesso();
    }
}