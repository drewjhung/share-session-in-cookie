package web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

import static session.SessionInCookieFilter.update;

/**
 * User: AKutuzov
 * Date: 8/7/13
 * Time: 12:30 PM
 */
@Controller
@RequestMapping("/")
public class HelloController {
    @RequestMapping("/")
    public String info(HttpSession session, Model model) {
        Integer counter = (Integer) session.getAttribute("counter");
        if (counter == null) {counter = 0;}
        counter++;
        session.setAttribute("counter", counter);
        model.addAttribute("counter", counter);
        update();
        return "index";
    }
}
