package com.mdd.booktor.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mdd.booktor.services.TurnoService;

@ExtendWith(MockitoExtension.class)
class FinalizarTurnosTaskTest {

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private FinalizarTurnosTask task;

    @Test
    void laTareaLlamaAlServiceParaFinalizarLosTurnosTerminados() {
        task.finalizarTurnosTerminados();

        verify(turnoService).finalizarTurnosTerminados();
    }
}
