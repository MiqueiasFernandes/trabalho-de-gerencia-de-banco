<%-- 
    Document   : carga
    Created on : 29/07/2017, 00:23:08
    Author     : mfernandes
--%>

<%@page import="java.sql.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

        <%
            Connection con = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");  //Descobrindo o driver  
                out.println("Driver conectado");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/itgm", "root", "Mikefer@93");    //conectando com o banco  
                out.println("conexão realizada");
            } catch (ClassNotFoundException erroClass) {
                out.println("Class Drivernão foi localizado, erro = " + erroClass);
            } catch (SQLException e) {
                out.println("Erro na copnexão ao banco de dados, erro = " + e);
            }
            

     if (con != null) {
                Statement stmt = null;
                ResultSet result = null;

                try {
                    stmt = con.createStatement();
//                    rs = stmt.executeQuery("select login from jhi_user");

                    // or alternatively, if you don't know ahead of time that
                    // the query will be a SELECT...
                    if (stmt.execute("select * from jhi_user")) {
                        result = stmt.getResultSet();
                        while (result.next()) {
                            out.print(result.getString("login"));
                            out.println(result.getString("email") + "<br>");
                        }
                    }

                    // Now do something with the ResultSet ....
                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                } finally {
                    // it is a good idea to release
                    // resources in a finally{} block
                    // in reverse-order of their creation
                    // if they are no-longer needed

                    if (result != null) {
                        try {
                            result.close();
                        } catch (SQLException sqlEx) {
                        } // ignore

                        result = null;
                    }

                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException sqlEx) {
                        } // ignore

                        stmt = null;
                    }
                }
            }




        %>  

        <h1>Hello World!</h1>
        <button onclick="initrLive()">iniciar</button>

        <div id="msg"></div>
        
        <script>

            function initrLive() {
                var url = "ws://localhost:8080/trabalho5/status";

                console.log("tentando abrir conexão em " + url);

                websocket = new WebSocket(url);


                websocket.onopen = function () {
                    alert('aberto!');
                };
                
                websocket.onmessage = function(evt) {
                  document.getElementById("msg").innerHTML = evt.data;  
                }

            }


        </script>


    </body>
</html>
