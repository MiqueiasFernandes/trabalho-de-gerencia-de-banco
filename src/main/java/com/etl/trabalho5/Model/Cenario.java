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
public class Cenario {

    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<Integer> projetos = new ArrayList<>();

    Carregador carregador;

    public Cenario(Carregador carregador, java.sql.Statement stmt) {
        this.carregador = carregador;

        String query = "SELECT * FROM itgm2.Cenario";

        try {

            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                ids.add(result.getInt("id"));
                projetos.add(result.getInt("projeto"));
            }

        } catch (Exception ex) {
            carregador.printErro(ex.toString());
            carregador.printErro(query);
        }

    }

    public Object[] getCenario(int id) {

        for (int i = 0; i < ids.size(); i++) {

            if (ids.get(i) == id) {

                return new Object[]{
                    id,
                    projetos.get(i)
                };

            }

        }
        carregador.printErro("Cenario não encontrado: " + id);
        return null;

    }

}
