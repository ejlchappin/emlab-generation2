/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.gui;

import emlab.gen.engine.ScheduleWorker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 *
 * @author ejlchappin
 *
 * TODO see
 * https://jansipke.nl/websocket-tutorial-with-java-server-jetty-and-javascript-client/
 */
public class SimpleGUI2 extends ResourceHandler {

    public List<ScheduleWorker> scheduleWorkers;
    public List<ScheduleWorker> scheduleWorkersFinished;
    public boolean exit = false;
    public boolean stop = false;
    public String body;

    public SimpleGUI2() {
        super();
    }

    public SimpleGUI2(List<ScheduleWorker> scheduleWorkers, List<ScheduleWorker> scheduleWorkersFinished) {
        super();
        this.scheduleWorkers = scheduleWorkers;
        this.scheduleWorkersFinished = scheduleWorkersFinished;

    }

    @Override
    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException,
            ServletException {

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();

        if (target.equals("/stop")) {
            for (ScheduleWorker worker : scheduleWorkers) {
                worker.schedule.stop();
            }
            stop = true;
            printMenu(out, "stop");
        } else if (target.equals("/pause")) {
            for (ScheduleWorker worker : scheduleWorkers) {
                worker.schedule.pause();
            }
            printMenu(out, "pause");
        } else if (target.equals("/resume")) {
            for (ScheduleWorker worker : scheduleWorkers) {
                worker.schedule.resume();
            }
            printMenu(out, "resume");
        } else if (target.equals("/exit")) {
            for (ScheduleWorker worker : scheduleWorkers) {
                exit = true;
            }
            printMenu(out, "stop");
        } else if (target.equals("/")) {
            printMain(out);
        } else if (target.equals("/body")) {
            printBody(out);
        } else if (target.equals("/menu")) {
            printMenu(out, "");
        }
        baseRequest.setHandled(true);
    }

    public void printMain(PrintWriter out) {
        printFile("index.html", out);
    }

    public void printMenu(PrintWriter out, String state) {
        out.println("<h1>EMLab-gen2</h1>");
        if (state.equals("stop")) {
            out.println("<p><b>STOP</b> - <a href=\"pause\">PAUSE</a> - <a href=\"resume\">RESUME</a> - <a href=\"exit\">EXIT</a></p>");
        } else if (state.equals("pause")) {
            out.println("<p><a href=\"stop\">STOP</a> - <b>PAUSE</b> - <a href=\"resume\">RESUME</a>  - <a href=\"exit\">EXIT</a></p>");
        } else if (state.equals("resume")) {
            out.println("<p><a href=\"stop\">STOP</a> - <a href=\"pause\">PAUSE</a> - <b>RESUME</b> - <a href=\"exit\">EXIT</a></p>");
        } else {
            out.println("<p><a href=\"stop\">STOP</a> - <a href=\"pause\">PAUSE</a> - <b>RESUME</b> - <a href=\"exit\">EXIT</a></p>");
        }
    }

    public void printBody(PrintWriter out) {
        if (body != null) {
            out.println(body);
        }

        out.println("<h2>Active:</h2>");

        for (ScheduleWorker worker : scheduleWorkers) {
            out.println("<p>Iteration: " + worker.schedule.iteration + " at tick " + worker.schedule.getCurrentTick() + " - Engine state: " + worker.schedule.getState() + "</p>");
            out.println("<p>Emissions: " + worker.schedule.reps.calculateTotalEmissionsBasedOnPowerPlantDispatchPlans(false, worker.schedule.getCurrentTick()) + "</p>");
        }

        out.println("<h2>Finished:</h2>");
        for (ScheduleWorker worker : scheduleWorkersFinished) {
            out.println("<p>Iteration: " + worker.schedule.iteration + " at tick " + worker.schedule.getCurrentTick() + " - Engine state: " + worker.schedule.getState() + "</p>");
            out.println("<p>Emissions: " + worker.schedule.reps.calculateTotalEmissionsBasedOnPowerPlantDispatchPlans(false, worker.schedule.getCurrentTick()) + "</p>");
        }

    }

    public void printFile(String fileName, PrintWriter out) {
        
        InputStream in = getClass().getResourceAsStream("/web/" + fileName); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Scanner sc = null;
            sc = new Scanner(reader);

        // we just need to use \\Z as delimiter 
        sc.useDelimiter("\\Z");
        out.println(sc.next());

    }

}
