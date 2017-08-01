/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etl.trabalho5;

import com.etl.trabalho5.Model.Card;
import com.etl.trabalho5.Model.Cenario;
import com.etl.trabalho5.Model.Modelo;
import com.etl.trabalho5.Model.Prognose;
import com.etl.trabalho5.Model.Projeto;
import com.etl.trabalho5.Model.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.websocket.Session;

/**
 *
 * @author mfernandes
 */
public class Carregador {

    private Connection conexao = null, conexao2 = null;
    private final Session sessao;
    int registros = 0;

    public Carregador(Session sessao) {
        this.sessao = sessao;
    }

    public void inicializar(String usuario, boolean load, String senha) {
        status1(0);
        status2(0);

        try {
//            printMessage("Descobrindo o driver...", true);
            Class.forName("oracle.jdbc.driver.OracleDriver");
//            Class.forName("com.mysql.cj.jdbc.Driver");
            printMessage("Driver conectado", false);
            status1(50);
            printMessage("Conectando ao banco...", true);
            conexao = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", usuario, senha);
//            conexao = DriverManager.getConnection("jdbc:mysql://localhost:3306/itgm2", usuario, senha);
            if (!load) {
                conexao2 = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", usuario, senha);
//                conexao2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/Multidimensional", usuario, senha);
            }
            printMessage("conexão realizada", false);
            status1(100);
            status2(10);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cont = 0;
                    while (conexao != null) {
                        printTime(cont++);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            printErro(ex.getMessage());
                        }
                    }
                }
            }).start();

        } catch (ClassNotFoundException erroClass) {
            printErro("Class Drivernão foi localizado, erro = " + erroClass);
        } catch (SQLException e) {
            printErro("Erro na copnexão ao banco de dados, erro = " + e);
        }
    }

    public void etl(boolean modoRapido) {
        Integer id = 0;

        ArrayList<String> insertP = new ArrayList<>();
        ArrayList<String> insertM = new ArrayList<>();
        ArrayList<String> insertT = new ArrayList<>();
        ArrayList<String> insertU = new ArrayList<>();

        try {
            int tempoIdIncrement = 1;
            int cont = 0;
            java.sql.Statement stmt = null, stmt2 = null;
            ResultSet result = null;
            stmt = conexao.createStatement();
            stmt2 = conexao2.createStatement();

            status2(15);
            printMessage("obtendo prognoses...", true);
            Prognose prognose = new Prognose(this, stmt);
            status2(20);
            printMessage("obtendo modelos...", true);
            Modelo modelo = new Modelo(this, stmt);
            status2(25);
            printMessage("obtendo usuarios...", true);
            User user = new User(this, stmt);
            status2(30);
            printMessage("obtendo cards...", true);
            Card card = new Card(this, stmt);

            int quantidade = prognose.getQuantidade();
            int total = 0;
            float inc1 = 30.000f / quantidade, stat1 = 30.0001f;

            printMessage("sumarizando..", true);

            for (Integer id2 : prognose.getIds()) {
                id = id2;

                Object[] prog = prognose.getPrognose(id);

                status1(0);

                String relatorio = (String) prog[1];
//                int cen = (int) prog[2];

                printMessage("prognose " + id, false);
                status1(10);

                if (relatorio.length() < 3) {
                    cont++;
                    stat1 += inc1;
                    printErro("relatorio invalido: " + relatorio + " prognose id " + id);
                    continue;
                }

                int[] tempo = obterTempo(id, card);
                status1(20);

                Object[] usuario = user.getUsuario((int) prog[3]);
                ///obterUsuario(cen, user, projeto, cenario);
                status1(30);

                String[] sumarize = sumarizePrognose(relatorio);
                int numeroArquivos = sumarize.length;
                total += numeroArquivos;
                status1(40);

                float stat = 50.000f;
                float inc = (60.000f / numeroArquivos) / 5.00001f;

                for (String model : sumarize) {
                    Object[] mod = obterModelo(model, modelo);
                    status1(Math.round((stat += inc)));

                    String[] queryes = processaPrognose(
                            (int) mod[0],
                            (String) mod[2],
                            model,
                            tempoIdIncrement,
                            tempo[0],
                            tempo[1],
                            tempo[2],
                            tempo[3],
                            (int) usuario[0],
                            (String) usuario[1],
                            (String) usuario[2],
                            (String) usuario[3],
                            numeroArquivos,
                            (modoRapido ? null : stmt2),
                            stat,
                            inc
                    );

                    if (modoRapido) {
                        insertP.add(queryes[0]);
                        insertM.add(queryes[1]);
                        insertU.add(queryes[2]);
                        insertT.add(queryes[3]);
                    }

                }

                printAdicional(" " + ++cont + " de " + quantidade + " total " + total);
                status2(Math.round(stat1 += inc1));
                tempoIdIncrement++;

            }

            sendMessage("{\"adicional\":\" total " + total + "\"}", false);

            quantidade = insertM.size();

            if (modoRapido) {

                status1(0);
                printMessage("inserindo prognoses...", true);
                inserirVarios("multidimensional.Prognose", "(IDTempo, IDModelo, IDUsuario, NumeroArquivos)", insertP, stmt2,
                        "INSERT INTO multidimensional.Prognose (IDTempo, IDModelo, IDUsuario, NumeroArquivos) VALUES (",
                        (float) (100.0000 / quantidade), 60, (float) (10.000 / quantidade));

                status1(0);
                printMessage("inserindo modelos...", true);
                inserirVarios("multidimensional.Modelo", "(IDModelo, NomeModelo, Formula)", insertM, stmt2,
                        "INSERT INTO multidimensional.Modelo (IDModelo, NomeModelo, Formula) VALUES (",
                        (float) (100.000 / quantidade), 70, (float) (10.0000 / quantidade));

                status1(0);
                printMessage("inserindo usuarios...", true);
                inserirVarios("multidimensional.Usuario", "(IDUsuario, NomeUsuario, Login, Idioma)", insertU, stmt2,
                        "INSERT INTO multidimensional.Usuario (IDUsuario, NomeUsuario, Login, Idioma) VALUES (",
                        (float) (100.000 / quantidade), 80, (float) (10.000 / quantidade));

                status1(0);
                printMessage("inserindo tempos...", true);
                inserirVarios("multidimensional.Tempo", "(IDTempo, Ano, Mes, Dia, Hora)", insertT, stmt2,
                        "INSERT INTO multidimensional.Tempo (IDTempo, Ano, Mes, Dia, Hora) VALUES (",
                        (float) (100.0000 / quantidade), 90, (float) (10.000 / quantidade));

            }
            sendMessage("{\"caso\":1,\"status\":100}", false);
            sendMessage("{\"caso\":2,\"status\":100}", false);
            printMessage("concluindo...", true);

        } catch (Exception e) {
            printErro("Erro ao processar: " + id + " : " + e);
        } finally {
            try {
                Thread.sleep(1000);
                encerrar();
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

    public String[] processaPrognose(
            int idmodelo,
            String formula,
            String nomeModelo,
            int idTempo,
            int ano,
            int mes,
            int dia,
            int hora,
            int idusuario,
            String nomeusuario,
            String login,
            String idioma,
            int numeroArquivos,
            java.sql.Statement stmt,
            float status,
            float inc) {

        String Query1 = "INSERT INTO multidimensional.Prognose (IDTempo, IDModelo, IDUsuario, NumeroArquivos)"
                + " VALUES ('" + idTempo + "', '" + idmodelo + "', '" + idusuario + "', '" + numeroArquivos + "');";
        String Query2 = "INSERT INTO multidimensional.Modelo (IDModelo, NomeModelo, Formula) "
                + "VALUES ('" + idmodelo + "', '" + nomeModelo + "', '" + formula + "');";
        String Query3 = "INSERT INTO multidimensional.Usuario (IDUsuario, NomeUsuario, Login, Idioma) "
                + "VALUES ('" + idusuario + "', '" + nomeusuario + "', '" + login + "', '" + idioma + "');";
        String Query4 = "INSERT INTO multidimensional.Tempo (IDTempo, Ano, Mes, Dia, Hora) "
                + "VALUES ('" + idTempo + "', '" + ano + "', '" + mes + "', '" + dia + "', '" + hora + "');";

        if (stmt != null) {
            try {
                stmt.executeUpdate(Query1);
                status1(Math.round(status + (inc * 2)));
            } catch (Exception ex) {
                printErro(ex.toString());
            }
            try {
                stmt.executeUpdate(Query2);
                status1(Math.round(status + (inc * 3)));
            } catch (Exception ex) {
                printErro(ex.toString());
            }
            try {
                stmt.executeUpdate(Query3);
                status1(Math.round(status + (inc * 4)));
            } catch (Exception ex) {
                printErro(ex.toString());
            }
            try {
                stmt.executeUpdate(Query4);
                status1(Math.round(status + (inc * 5)));
            } catch (Exception ex) {
                printErro(ex.toString());
            }
        }

        return new String[]{Query1, Query2, Query3, Query4};
    }

    public Object[] obterUsuario(int idC, User usuario, Projeto projeto, Cenario cenario) {
        return usuario.getUser(idC, projeto, cenario);
    }

    public int[] obterTempo(int idPrognose, Card card) {
        return card.getTempo(idPrognose);
    }

    public Object[] obterModelo(String nome, Modelo modelo) {
        return modelo.getMoveloByNome(nome);
    }

    public String[] sumarizePrognose(String relatorio) {
        relatorio = relatorio.substring(2, relatorio.length() - 6);
        return relatorio.replace("\":{},\"", "\n").split("\n");
    }

    public void carregar(int prognoses, String casos, boolean rapido) {

        String[] usuarios = null, modelos = null, cards = null, prognos = null;
        status2(10);

        if (casos.contains("u")) {
            printMessage("obtendo usuarios...", true);
            usuarios = GeradorData.getUsers(1000);
        }

        if (casos.contains("m")) {
            status2(20);
            printMessage("obtendo modelos...", true);
            modelos = GeradorData.getModelos();
        }

        if (casos.contains("c")) {
            status2(30);
            printMessage("obtendo cards...", true);
            cards = GeradorData.getCards(prognoses);
        }

        if (casos.contains("p")) {
            status2(40);
            printMessage("obtendo prognoses...", true);
            prognos = GeradorData.getPrognoses(prognoses);
        }

        if (casos.contains("u")) {
            status2(50);
            printMessage("inserindo usuarios...", true);
            insert("Usuario", "itgm2.Usuario", "id,first_name,last_name,login,language_key", usuarios, true, false, rapido, 50, 60);
        }

        if (casos.contains("m")) {
            status2(60);
            printMessage("inserindo modelos...", true);
            insert("Modelo", "itgm2.Modelo", "id,nome,formula", modelos, true, false, rapido, 60, 70);
        }

        if (casos.contains("c")) {
            status2(70);
            printMessage("inserindo cards...", true);
            insert("Card", "itgm2.Card", "id,meta", cards, false, false, rapido, 70, 80);
        }

        if (casos.contains("p")) {
            status2(80);
            printMessage("inserindo prognoses...", true);
            insert("Prognose", "itgm2.Prognose", "id,relatorio,cenario", prognos, false, true, rapido, 80, 99);
        }

        status2(100);
        printMessage("encerrando...", true);
        encerrar();

    }

    void insert(String tabela, String tabela2, String colunas, String[] valores, boolean sep, boolean prog, boolean rapido, int min, int max) {
        String Query = null;
        int countProj = 1, countCen = 1;
        ArrayList<String> queries = new ArrayList<>();
        ArrayList<String> queries1 = new ArrayList<>();
        ArrayList<String> queries2 = new ArrayList<>();
        try {
            status1(0);
            java.sql.Statement stmt = conexao.createStatement();
            status1(1);
            float inc = (float) (100.00 / valores.length);
            float inc2 = (float) (((max - min + 0.000001) / valores.length) / (prog ? 3 - 0.000001 : 1 - 0.000001));
//            inc = !prog ? inc : inc / 3;
            float stat = 0, stat2 = min;
            String query = "INSERT INTO itgm2." + tabela + " (" + colunas + ") VALUES (";
            String query2 = ", '";
            String query3 = "', '";
            String query4 = "');";
            int count = 1;
            for (String valor : valores) {
                String[] data;
                if (sep) {
                    data = valor.split(",");
                } else {
                    data = new String[]{valor.split(",")[0], valor.substring(valor.indexOf(",") + 1)};
                    if (prog) {
                        data = new String[]{
                            data[0],
                            data[1].substring(0, data[1].lastIndexOf(",")),
                            data[1].substring(data[1].lastIndexOf(",") + 1)
                        };
                    }
                }
                String cons = query + data[0] + query2;
                for (int i = 1; i < data.length; i++) {
                    cons += (!prog ? data[i] : (i == 2 ? countCen : data[1])) + (i < (data.length - 1) ? query3 : "");
                }

                Query = cons + query4;

                if (!rapido) {
                    stmt.executeUpdate(Query);
                } else {
                    queries.add(Query);
                }

                if (prog) {

                    Query = "INSERT INTO itgm2.Projeto (id,usuario) VALUES (" + countProj + "," + data[2] + ");";
                    if (!rapido) {
                        stmt.executeUpdate(Query);
                    } else {
                        queries1.add(Query);
                    }

                    Query = "INSERT INTO itgm2.Cenario (id,projeto) VALUES (" + countCen + "," + countProj + ");";
                    if (!rapido) {
                        stmt.executeUpdate(Query);
                    } else {
                        queries2.add(Query);
                    }

                    countCen++;
                    countProj++;
                }

                if (!rapido) {
                    status1(Math.round((stat += inc)));
                    status2(Math.round((stat2 += inc2)));
                    printMessage("inserindo " + count++ + " de " + valores.length, false);
                }
            }

            inserirVarios(tabela2, "(" + colunas + ")", queries, stmt, query, inc, min, inc2);

            if (prog) {
                printMessage("inserindo projetos...", true);
                inserirVarios("itgm2.Projeto", "(id,usuario)", queries1, stmt, "INSERT INTO itgm2.Projeto (id,usuario) VALUES (", inc, min + Math.round(inc2 * valores.length), inc2);

                printMessage("inserindo cenarios...", true);
                inserirVarios("itgm2.Cenario", "(id,projeto)", queries2, stmt, "INSERT INTO itgm2.Cenario (id,projeto) VALUES (", inc, min + Math.round(inc2 * valores.length * 2), inc2);
            }

        } catch (SQLException ex) {
            printErro(ex.getMessage());
            printErro(Query);
        }
    }

    void inserirVarios(
            String tabela,
            String campos,
            ArrayList<String> queries,
            java.sql.Statement stmt,
            String query,
            float inc,
            int min,
            float inc2) {

        status1(0);
        status2(min);

        String Query = null;
        int length = queries.size();
        float stat = 0, min2 = min;
        try {
            int count = 0;
            while ((length - count) > 0) {
                int qtd = Integer.min(1000, (length - count));
                Query = "INSERT ALL " + query.substring(7, query.length() - 1)
                        + String.join("\n", queries.subList(count, count + qtd).toArray(new String[]{}))
                                .replace(query.substring(0, query.length() - 2), "")
                                .replaceAll(";\n", " INTO " + tabela + " " + campos + " VALUES ")
                                .replace(";", "") + " SELECT * FROM dual";
                stmt.executeUpdate(Query);
                status1(Math.round((stat += (inc * qtd))));
                status2(Math.round((min2 += (inc2 * qtd))));
                printMessage("inserindo " + (count += qtd) + " de " + length, false);
                sendMessage("{\"registros\":" + registros++ + "}", false);
            }
        } catch (SQLException ex) {
            printErro(ex.getMessage());
            printErro(Query);
        }
    }

    public void encerrar() {
        status1(100);
        status2(100);
        try {
            conexao.close();
            if (conexao2 != null) {
                conexao2.close();
            }
        } catch (SQLException ex) {
            printErro(ex.getMessage());
        } finally {
            conexao = conexao2 = null;
        }
        if (sessao.isOpen()) {
            try {
                sessao.close();
            } catch (IOException ex) {
                System.err.println(ex + "Carregador[encerrar]");
            }
        }
    }

    public void printMessage(String message, boolean add) {
        sendMessage("{\"message\":\"" + message + "\",\"add\":" + add + "}", false);
    }

    public void status1(int percent) {
        printstatus(1, percent);
    }

    public void status2(int percent) {
        printstatus(2, percent);
    }

    void printstatus(int caso, int percent) {
        sendMessage("{\"caso\":" + caso + ",\"status\":" + percent + "}", caso == 1);
    }

    public void printErro(String erro) {
        sendMessage("{\"erro\":\"" + erro.replaceAll("\\s", " ") + "\"}", false);
    }

    void printTime(int time) {
        sendMessage("{\"time\":" + time + "}", false);
    }

    public void printAdicional(String text) {
        sendMessage("{\"adicional\":\"" + text + "\"}", true);
    }

    int threads = 0;

    void sendMessage(final String message, boolean perde) {

        if (this.sessao != null && this.sessao.isOpen()) {

            try {

                this.sessao.getAsyncRemote().sendText(message);
                ///System.out.println("ENVIADO: " + message);

            } catch (Exception ex) {
                if (!perde) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean erro = true;
                            while (erro) {
                                try {
                                    sessao.getAsyncRemote().sendText(message);
                                    erro = false;
                                } catch (Exception ex) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        System.err.println(e);
                                    }
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    }
}
