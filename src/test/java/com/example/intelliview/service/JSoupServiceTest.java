package com.example.intelliview.service;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@AutoConfigureMockMvc
@ContextConfiguration(classes = JSoupService.class)
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest
class JSoupServiceTest {

    @Autowired
    private JSoupService jSoupService;

    @Test
    void getPinnedRepositoryTest() throws IOException {
        String username = "minahkim03";
        System.out.println(jSoupService.getPinnedRepository(username));
    }


}