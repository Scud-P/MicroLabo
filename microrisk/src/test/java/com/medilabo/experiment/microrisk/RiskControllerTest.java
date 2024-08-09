package com.medilabo.experiment.microrisk;

import com.medilabo.experiment.microrisk.controller.RiskController;
import com.medilabo.experiment.microrisk.service.RiskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(RiskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RiskControllerTest {

    @Autowired
    private RiskController riskController;

    @MockBean
    private RiskService riskService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetRiskForPatient() throws Exception {
        String risk = "None";

        when(riskService.calculateRiskForPatient(anyLong())).thenReturn(risk);

        mockMvc.perform(get("/risk/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("None"));
    }
}
