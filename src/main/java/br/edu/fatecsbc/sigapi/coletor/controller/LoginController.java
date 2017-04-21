package br.edu.fatecsbc.sigapi.coletor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.edu.fatecsbc.sigapi.coletor.model.Login;
import br.edu.fatecsbc.sigapi.coletor.service.ColetorService;

@Controller
public class LoginController {

    @Autowired
    private ColetorService service;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loginForm(final Model model) {
        model.addAttribute("login", new Login());
        return "login";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String loginSubmit(@ModelAttribute final Login login, final Model model) {

        final boolean result = service.execute(login);
        model.addAttribute("result", result);

        return "result";

    }

}
