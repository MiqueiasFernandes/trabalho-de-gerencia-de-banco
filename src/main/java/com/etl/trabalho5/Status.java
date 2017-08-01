/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etl.trabalho5;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author mfernandes
 */
@ServerEndpoint("/status")
public class Status {

    Carregador carregador = null;

    @OnOpen
    public void onOpen(Session sessao) {
        carregador = new Carregador(sessao);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("recebido " + message);
        try {
            if (carregador != null) {
                String[] opcoes = message.split(":");
                switch (opcoes[0]) {
                    case "conectar":
                        carregador.inicializar(opcoes[1], "true".equals(opcoes[2]), opcoes[3]);
                        break;
                    case "carregar":
                        carregador.carregar(Integer.parseInt(opcoes[1]), opcoes[2], "true".equals(opcoes[3]));
                        break;
                    case "etl":
                        carregador.etl("true".equals(opcoes[1]));
                        break;
                    case "encerrar":
                        carregador.encerrar();
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("erro: " + ex);
        }
    }

    @OnClose
    public void onClose() {
        if (carregador != null) {
            carregador.encerrar();
        }
    }

}
