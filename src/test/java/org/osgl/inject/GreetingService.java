package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
