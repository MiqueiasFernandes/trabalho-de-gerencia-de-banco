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
public class Prognose {

    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> relatorios = new ArrayList<>();
    ArrayList<Integer> cenarios = new ArrayList<>();
    ArrayList<Integer> usuarios = new ArrayList<>();

    Carregador carregador;

    public Prognose(Carregador carregador, java.sql.Statement stmt) {
        this.carregador = carregador;

        String query = "SELECT * FROM itgm.Prognose;";
        query = "select tbupc.iduser as user, relatorio, id, cenario  from Prognose inner join ( \n"
                + "select id as idC, projeto, tbpu.idus as iduser from Cenario inner join ( \n"
                + "select id as idP, tbu.idus as idus from Projeto inner join ( select id as idus from User ) tbu on tbu.idus = usuario ) tbpu \n"
                + "on tbpu.idP = projeto ) tbupc\n"
                + "on tbupc.idC = cenario;";

        query = "select id, relatorio, cenario, usuario from itgm2.Prognose inner join ("
                + "select id as idcen, usuario from itgm2.Cenario inner join  ( "
                + "select Usuario.id as usuario, Projeto.id as projeto from "
                + "itgm2.Projeto inner join itgm2.Usuario on Projeto.usuario = Usuario.id )"
                + " tb3 on tb3.projeto = Cenario.projeto) tb4 on tb4.idcen = cenario";

        query = "select prognose.id, relatorio, usuario.id as usuario, cenario from itgm2.prognose join"
                + " itgm2.cenario on prognose.cenario = cenario.id "
                + "join itgm2.projeto on cenario.projeto = projeto.id "
                + "join itgm2.usuario on projeto.usuario = usuario.id";
        try {
////select count(*) from itgm2.prognose join itgm2.cenario on prognose.cenario = cenario.id join itgm2.projeto on cenario.projeto = projeto.id join itgm2.usuario on projeto.usuario = usuario.id;

            ResultSet result = stmt.executeQuery(query);
//carregador.printMessage("consulta realizada...", true);

            while (result.next()) {
                ids.add(result.getInt("id"));
                cenarios.add(result.getInt("cenario"));
                relatorios.add(result.getString("relatorio").toString());
                usuarios.add(result.getInt("usuario"));
            }
//carregador.printMessage("dados armz...", true);

        } catch (Exception ex) {
            carregador.printErro(ex.toString());
            carregador.printErro(query);
        }

    }

    public ArrayList<Integer> getIds() {
        return this.ids;
    }

    public Object[] getPrognose(int id) {

        for (int i = 0; i < ids.size(); i++) {
//        int i = id + 1;

            if (ids.get(i) == id) {

                return new Object[]{
                    id,
                    relatorios.get(i),
                    cenarios.get(i),
                    usuarios.get(i)
                };

            }

        }
        carregador.printErro("Prognose não encontrado: " + id);
        return null;

    }

    public int getQuantidade() {
        return ids.size();
    }

}
