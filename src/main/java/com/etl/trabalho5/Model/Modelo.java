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
public class Modelo {

    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> nomes = new ArrayList<>();
    ArrayList<String> formulas = new ArrayList<>();

    Carregador carregador;

    public Modelo(Carregador carregador, java.sql.Statement stmt) {
        this.carregador = carregador;

        String query = "SELECT * FROM itgm2.Modelo";

        try {

            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                ids.add(result.getInt("id"));
                nomes.add(result.getString("nome"));
                formulas.add(result.getString("formula"));
            }

        } catch (Exception ex) {
            carregador.printErro(ex.toString());
            carregador.printErro(query);
        }

    }

    public Object[] getModelo(int id) {
//int i = id + 1;
        for (int i = 0; i < ids.size(); i++) {

            if (ids.get(i) == id) {

                return new Object[]{
                    id,
                    nomes.get(i),
                    formulas.get(i)
                };

            }

        }
        carregador.printErro("Modelo não encontrado id: " + id);
        return null;
    }

    public Object[] getModeloByIndex(int index) {
        return getModelo(ids.get(index));
    }

    public Object[] getMoveloByNome(String nome) {
        for (int i = 0; i < nomes.size(); i++) {

            if (nome.equals(nomes.get(i))) {
                return getModeloByIndex(i);
            }
        }
        carregador.printErro("Modelo não encontrado nome: " + nome);
        return null;
    }

}
