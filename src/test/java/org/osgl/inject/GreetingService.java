package org.osgl.inject;

public class GreetingService {

    @Uppercase("hElLo WoRlD")
    private String upper;

    @Lowercase("hElLo WoRlD")
    private String lower;

    public String sayHello(String caller) {
        return String.format("%s (%s), %s", upper, lower, caller);
    }

    public static void main(String[] args) {
        Genie genie = Genie.create();
        GreetingService service = genie.get(GreetingService.class);
        System.out.println(service.sayHello("Genie"));
    }

}
