/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.QR.Code;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
class TaskService {
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;
    private final AtomicReference<String> ultimoNumero = new AtomicReference<>("");
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public TaskService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void startTask(long minuti) {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }

        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                String numeri = String.valueOf(generaNum());
                ultimoNumero.set(numeri);

                for (SseEmitter emitter : emitters) {
                    try {
                        emitter.send(SseEmitter.event().data(numeri));
                    } catch (Exception e) {
                        emitters.remove(emitter);
                    }
                }
            }
        };
        task.run();
        future = taskScheduler.scheduleAtFixedRate(task, TimeUnit.MINUTES.toMillis(minuti));
    }
    
    public void registerEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        try {
        String ultimo = ultimoNumero.get();
        if (ultimo == null || ultimo.isEmpty()) {
            ultimo = String.valueOf(generaNum()); // genera subito un numero se non esiste
            ultimoNumero.set(ultimo);
        }
        emitter.send(SseEmitter.event().data(ultimo));
    } catch (Exception e) {
        emitters.remove(emitter);
    }
    }
    
    public String getUltimoNumero() {
        String numero = ultimoNumero.get();
        return numero.isEmpty() ? "Nessun numero generato ancora!" : numero;}

    private int generaNum() {
        Random rand = new Random();
        return rand.nextInt(99999999) + 1;
    }
}
