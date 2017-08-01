/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etl.trabalho5.Model;

import com.etl.trabalho5.Carregador;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author mfernandes
 */
public class User {

    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> nm = new ArrayList<>();
    ArrayList<String> lg = new ArrayList<>();
    ArrayList<String> lk = new ArrayList<>();

    Carregador carregador;

    public User(Carregador carregador, java.sql.Statement stmt) {
        this.carregador = carregador;

        String query = "SELECT * FROM itgm2.Usuario";

        try {

            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                ids.add(result.getInt("id"));
                nm.add(result.getString("first_name") + " " + result.getString("last_name"));
                lg.add(result.getString("login"));
                lk.add(result.getString("language_key"));
            }

        } catch (Exception ex) {
            carregador.printErro(ex.toString());
            carregador.printErro(query);
        }

    }

    public Object[] getUsuario(int id) {
//        int i = id + 1;
        for (int i = 0; i < ids.size(); i++) {

        if (ids.get(i) == id) {

            return new Object[]{
                id,
                nm.get(i),
                lg.get(i),
                lk.get(i)
            };

        }

        }
        carregador.printErro("Usuario não encontrado: " + id);
        return null;

    }

    public Object[] getUser(int idC, Projeto projeto, Cenario cenario) {
        return getUsuario((int) projeto.getProjeto((int) cenario.getCenario(idC)[1])[1]);
    }

}
