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
public class Card {

    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> metas = new ArrayList<>();

    Carregador carregador;

    public Card(Carregador carregador, java.sql.Statement stmt) {
        this.carregador = carregador;

        String query = "SELECT * FROM itgm2.Card";

        try {

            ResultSet result = stmt.executeQuery(query);

            while (result.next()) {
                ids.add(result.getInt("id"));
                metas.add(result.getString("meta"));
            }

        } catch (Exception ex) {
            carregador.printErro(ex.toString());
            carregador.printErro(query);
        }

    }

    public Object[] getCard(int id) {

        for (int i = 0; i < ids.size(); i++) {

            if (ids.get(i) == id) {

                return new Object[]{
                    id,
                    metas.get(i)
                };

            }

        }
        carregador.printErro("Card não encontrado1: " + id);
        return null;

    }

    public Object[] getCardByPrognose(int idP) {
        String query = "{\"prognose\":" + idP + ",\"data";

        for (int i = 0; i < metas.size(); i++) {

            if (metas.get(i).startsWith(query)) {
                return getCard(ids.get(i));
            }
        }
        carregador.printErro("Card não encontrado2: " + idP);
        return null;

    }

    public int[] getTempo(int idP) {
        String meta = (String) getCardByPrognose(idP)[1];

        meta = meta.substring(meta.indexOf(",\"data\":\"") + 9).replace("\"}", "");
        String[] data = meta.split(" ")[0].split("-");
        String[] hora = meta.split(" ")[1].split(":");

        return new int[]{
            Integer.parseInt(data[0]),
            Integer.parseInt(data[1]),
            Integer.parseInt(data[2]),
            Integer.parseInt(hora[0]),
            Integer.parseInt(hora[1])
        };
    }

}
