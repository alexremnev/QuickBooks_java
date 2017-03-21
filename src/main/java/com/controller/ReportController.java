package com.controller;

import com.intuit.ipp.exception.FMSException;
import com.service.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private Persistable[] services;

    @Autowired
    public ReportController(Persistable... services) {

        this.services = services;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/calculate.htm")
    public String calculate() {
        try {
            calculateDocuments();
            return "index";
        } catch (Exception e) {
            logger.error("Exception occured when you tried to recalculate sales tax", e.getCause());
            return "exception";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save.htm")
    public String save(HttpServletResponse response) throws IOException {
        try {
            saveData();
        } catch (Exception e) {
            logger.error("Exception occured when you tried to pull entity", e.getCause());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return "index";
    }

    private void calculateDocuments() {
        for (Persistable service : services) {
            service.calculate();
        }
    }

    private void saveData() throws FMSException {
        for (Persistable service : services) {
            service.save();
        }
    }
}