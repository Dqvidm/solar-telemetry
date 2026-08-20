package com.example.solartelemetry.controller;

import com.example.solartelemetry.model.PanelTelemetry;
import com.example.solartelemetry.service.PanelTelemetryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private final PanelTelemetryService service;

    public WebController(PanelTelemetryService service) {
        this.service = service;
    }

    @GetMapping("/dashboard-legacy")
    public String viewHomePage(Model model) {
        model.addAttribute("telemetryList", service.getAllTelemetry());
        return "index";
    }

    @PostMapping("/add-legacy")
    public String addPanelTelemetry(@ModelAttribute PanelTelemetry telemetry) {
        service.saveTelemetry(telemetry);
        return "redirect:/dashboard-legacy";
    }
}