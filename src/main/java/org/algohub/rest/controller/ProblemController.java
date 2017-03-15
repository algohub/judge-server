package org.algohub.rest.controller;

import org.algohub.engine.pojo.Problem;
import org.algohub.rest.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @RequestMapping(method = RequestMethod.GET, value = "/problems/{id}")
    public Problem get(@PathVariable("id") String problemId) {
        return problemService.getProblemById(problemId);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/problems")
    public void add(@RequestBody String jsonStr) {
        problemService.addProblem(jsonStr);
    }
}
