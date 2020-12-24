package com.cisco.webex.service.common.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is purely an example just to keep the controller part separate from the main application. It could,
 * for the purposes of this example, have been combined ...
 */
@RestController
public class RateLimitExampleController {

    @RequestMapping("/hello")
    public String hello(@RequestParam(required = false) String name)
    {
        return "Hello, " + ((name == null) ? "guest": name) + "\n";
    }

    @RequestMapping("/goodbye")
    public String goodbye(@RequestParam(required = false) String name)
    {
        return "Goodbye, " + ((name == null) ? "guest": name) + "\n";
    }
}
