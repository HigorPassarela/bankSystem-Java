package br.com.banksystem.fraudes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CamundaDeploymentRunner")
class CamundaDeploymentRunnerTest {

    @Mock
    private CamundaDeploymentService camundaDeploymentService;

    private CamundaDeploymentRunner camundaDeploymentRunner;

    @BeforeEach
    void setUp() {
        camundaDeploymentRunner = new CamundaDeploymentRunner(camundaDeploymentService);
    }

    @Test
    @DisplayName("Deve chamar deployarProcesso ao executar run")
    void deveChamarDeployarProcessoAoExecutarRun() throws Exception {
        // When
        camundaDeploymentRunner.run();

        // Then
        verify(camundaDeploymentService, times(1)).deployarProcesso();
    }

    @Test
    @DisplayName("Deve chamar deployarProcesso ao executar run com argumentos")
    void deveChamarDeployarProcessoAoExecutarRunComArgumentos() throws Exception {
        // Given
        String[] args = {"arg1", "arg2"};

        // When
        camundaDeploymentRunner.run(args);

        // Then
        verify(camundaDeploymentService, times(1)).deployarProcesso();
    }
}