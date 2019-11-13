/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import org.json.simple.parser.*;
import java.io.*;
import org.json.simple.JSONObject;
import com.automotriz.Datos.GestorDB;
import com.automotriz.logger.Logger;
import java.sql.Connection;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.*;
import com.automotriz.Datos.Mail;
import com.automotriz.VO.SessionVO;

public class Peticiones_test {

    private String query;
    private JSONObject request;
    private String datadic;
    private Object objVO;

    public Peticiones_test(JSONObject request) {
        this.request = request;
        this.datadic = request.get("datadic").toString();
        createNewRequest();
    }

    public Peticiones_test() {
    }

    public void setObjectVO(Object objVO) {
        this.objVO = objVO;
    }

    public static Connection requestSQLConnection() {
        return GestorDB.sendSQLConnection();
    }

    /**
     * All the constants that are related to the queries names
     */
    enum DATADICS {
        VALIDATEUSER("S001_VALIDATEUSER"),
        BLOCKUSER("S001_BLOCKUSER"),
        VALIDATEUSERNAME("S002_VALIDATEUSERNAME"),
        CREATENEWUSER("S002_CREATENEWUSER"),
        VALIDATEADMIN("S002_VALIDATEADMIN"),
        UPDATEUSER("S003_UPDATEUSER"),
        REMOVEUSER("S003_REMOVEUSER"),
        FILTRARUSUARIOS("S004_FILTRARUSUARIOS"),
        FILTRARTODOSUSUARIOS("S004_FILTRARTODOSUSUARIOS"),
        ACTIVARUSER("S005_ACTIVARUSER"),
        INSERTNEWAUTO("S006_INSERTNEWAUTO"),
        GETCARS("S006_GETCARS"),
        UPDATEAUTO("S006_UPDATEAUTO"),
        SUBMITCOMENTARIO("S007_SUBMITCOMENTARIO"),
        GETFEEDBACK("S008_GETFEEDBACK"),
        GETCATALOGO("S009_GETCATALOGO"),
        GETVENDEDOR("S009_GETVENDEDOR"),
        GETVENDEDORNAME("S010_GETVENDEDORNAME"),
        FILTRARAUTOS("S011_FILTRARAUTOS"),
        VALIDATEUSERTEST("S001_VALIDATEUSERTEST");

        private final String value;

        private DATADICS(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    };

    /**
     * Sends an email to destinatarios with a attached file
     *
     * @param destinatarios
     * @param asunto
     * @param mensaje
     * @param objFile
     */
    public static void sendMail(String destinatarios, String asunto, String mensaje, Object objFile) {
        Mail mail = new Mail(destinatarios, asunto, mensaje);
        if (objFile != null) {
            File file = (File) objFile;
            mail.attachFiles(file.getName(), file.getAbsolutePath());
        }
        mail.send();
    }

    /**
     * Creates a new request to the server
     *
     * @param datadic The datadic that contains the query
     */
    private void createNewRequest() {
        this.query = readOperationsIdentifier();
    }

    /**
     * @return Returns the JSON response generated by GestorDB
     */
    public JSONObject getResult() {
        Logger.log("Getting JSON result");
        return sendToGestor(this.query);
    }

    private String readOperationsIdentifier() {
        JSONParser jsonParser = new JSONParser();
        try {
            Logger.log("Reading OperationsIdentifier file");
            FileReader reader = new FileReader("OperationsIdentifier.json");
            JSONObject operations = (JSONObject) jsonParser.parse(reader);
            String[] sqlData = null;
            String sql = "";
            switch (datadic) {
                case "VALIDATEUSER":
                    sqlData = VALIDATEUSER(operations, request);
                    break;
                case "BLOCKUSER":
                    sqlData = BLOCKUSER(operations, request);
                    break;
                case "VALIDATEUSERNAME":
                    sqlData = VALIDATEUSERNAME(operations, request);
                    break;
                case "CREATENEWUSER":
                    sqlData = CREATENEWUSER(operations, request);
                    break;
                case "VALIDATEADMIN":
                    sqlData = VALIDATEADMIN(operations, request);
                    break;
                case "UPDATEUSER":
                    sqlData = UPDATEUSER(operations, request);
                    break;
                case "REMOVEUSER":
                    sqlData = REMOVEUSER(operations, request);
                    break;
                case "FILTRARUSUARIOS":
                    sqlData = FILTRARUSUARIOS(operations, request);
                    break;
                case "FILTRARTODOSUSUARIOS":
                    sqlData = FILTRARTODOSUSUARIOS(operations, request);
                    break;
                case "ACTIVARUSER":
                    sqlData = ACTIVARUSER(operations, request);
                    break;
                case "INSERTNEWAUTO":
                    //gather all the new images path
                    getImagesFromHashMap(request);
                    sqlData = INSERTNEWAUTO(operations, request);
                    break;
                case "GETCARS":
                    sqlData = GETCARS(operations, request);
                    break;//S006_UPDATEAUTO
                case "UPDATEAUTO":
                    getImagesFromHashMap(request);
                    sqlData = UPDATEAUTO(operations, request);
                    break;
                case "SUBMITCOMENTARIO":
                    sqlData = SUBMITCOMENTARIO(operations, request);
                    break;
                case "GETFEEDBACK":
                    sqlData = GETFEEDBACK(operations);
                    break;
                case "GETCATALOGO":
                    sqlData = GETCATALOGO(operations, request);
                    break;
                case "GETVENDEDOR":
                    sqlData = GETVENDEDOR(operations, request);
                    break;
                case "GETVENDEDORNAME":
                    sqlData = GETVENDEDORNAME(operations, request);
                    break;
                case "FILTRARAUTOS":
                    sqlData = FILTRARAUTOS(operations, request);
                    break;
                case "VALIDATEUSERTEST":
                    sql = VALIDATEUSERTEST(operations);
            }
            //returns the query from operationsIdentifier
            return formQuery(sql);
        } catch (Exception e) {
            Logger.error(e.toString());
            Logger.error(e.getStackTrace());
            return "";
        }
    }

    private JSONObject getImagesFromHashMap(JSONObject request) throws Exception {
        HashMap imagesPath = (HashMap) request.get("9");
        String newImagesPath = new String();
        for (Object pathObj : imagesPath.values().toArray()) {
            String currentPath = "Catalogo/" + new File(pathObj.toString()).getName();

            if (!new File(currentPath).exists()) {
                String newPath = "Catalogo/" + request.get("10") + "-" + new File(pathObj.toString()).getName();
                newImagesPath += newPath + ";";
                Files.copy(
                        Paths.get(new File(pathObj.toString()).getAbsolutePath()),
                        Paths.get(newPath));
            } else {
                newImagesPath += currentPath + ";";
            }
        }
        request.put("9", newImagesPath);
        return request;
    }

    /**
     * Sends the query to GestorDB extracted from the OperationsIdentifier
     *
     * @param query The SQL query statement
     * @return A JSON object generated by GestorDB as a response
     */
    private JSONObject sendToGestor(String query) {
        Logger.log("Sending query to GestorDB");
        GestorDB gestor = new GestorDB();
        gestor.putQuery(query);
        gestor.setObjectVO(objVO);
        //executing the query
        JSONObject response = gestor.executeQuery(request);
        return response;
    }

    private String formQuery(String[] data) {
        Logger.log("Forming query got from datadic");
        String query = "";
        for (String dt : data) {
            query += dt + " ";
        }
        return query.trim();
    }

    private String formQuery(String sql) {
        HashMap<String, String> data = (HashMap<String, String>) request.get("request");
        for (String key : data.keySet()) {
            String value = validateDataType(data.get(key));
            sql += key + "=" + value;
        }
        return sql;
    }

    private String validateDataType(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return value + "";
    }

    private static String[] VALIDATEUSER(JSONObject operations, JSONObject request) {
        DATADICS dic = DATADICS.VALIDATEUSER;
        JSONObject operationObject = (JSONObject) operations.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("colums"),
            (String) operationObject.get("from"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            (String) operationObject.get("field1"),
            "'" + request.get("1").toString() + "'",
            (String) operationObject.get("field2"),
            "'" + request.get("2").toString() + "'"
        };
    }

    private static String[] BLOCKUSER(JSONObject operations, JSONObject request) {
        DATADICS dic = DATADICS.BLOCKUSER;
        JSONObject operationObject = (JSONObject) operations.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("update"),
            (String) operationObject.get("table"),
            (String) operationObject.get("set"),
            (String) operationObject.get("where"),
            (String) operationObject.get("field"),
            "'" + request.get("1").toString() + "'"
        };
    }

    private static String[] VALIDATEUSERNAME(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.VALIDATEUSERNAME;
        JSONObject datadicObj = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) datadicObj.get("select"),
            (String) datadicObj.get("colums"),
            (String) datadicObj.get("from"),
            (String) datadicObj.get("table"),
            (String) datadicObj.get("where"),
            (String) datadicObj.get("field1"),
            "'" + request.get("1").toString() + "'"
        };
    }

    private static String[] CREATENEWUSER(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.CREATENEWUSER;
        JSONObject datadicObj = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) datadicObj.get("insert"),
            (String) datadicObj.get("table"),
            (String) datadicObj.get("columns"),
            (String) datadicObj.get("values"),
            "('" + request.get("1").toString() + "',"
            + "'" + request.get("2").toString() + "',"
            + "'" + request.get("3").toString() + "',"
            + "'" + request.get("4").toString() + "',"
            + "'ACTIVO',"
            + "'" + request.get("5").toString() + "',"
            + "'" + request.get("6").toString() + "')"
        };
    }

    private static String[] VALIDATEADMIN(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.VALIDATEADMIN;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("colums"),
            (String) operationObject.get("from"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            (String) operationObject.get("field1"),
            "'" + request.get("1").toString() + "'",
            (String) operationObject.get("field2"),
            "'" + request.get("2").toString() + "'"
        };
    }

    private static String[] UPDATEUSER(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.UPDATEUSER;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("update"),
            (String) operationObject.get("table"),
            (String) operationObject.get("set"),
            "usuario='" + request.get("1") + "' ,"
            + "contrasena='" + request.get("2") + "', "
            + "correo='" + request.get("3") + "', "
            + "perfil='" + request.get("4") + "', "
            + "telefono='" + request.get("5") + "',"
            + "nombre='" + request.get("6") + "'",
            (String) operationObject.get("where"),
            (String) operationObject.get("field1"),
            "'" + request.get("1").toString() + "'"
        };
    }

    private static String[] REMOVEUSER(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.REMOVEUSER;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("update"),
            (String) operationObject.get("table"),
            (String) operationObject.get("set"),
            (String) operationObject.get("values"),
            (String) operationObject.get("where"),
            (String) operationObject.get("field1"),
            "'" + request.get("1").toString() + "'"
        };
    }

    private static String[] FILTRARUSUARIOS(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.FILTRARUSUARIOS;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());
        //creating the query's conditions, so we get data from our filter view
        String sqlFilter = new Peticiones_test().creatingFilterUsuarios(request);

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("columns"),
            (String) operationObject.get("from"),
            (String) operationObject.get("where"),
            sqlFilter
        };
    }

    private static String[] FILTRARTODOSUSUARIOS(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.FILTRARTODOSUSUARIOS;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("columns"),
            (String) operationObject.get("from"),};
    }

    private static String[] ACTIVARUSER(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.ACTIVARUSER;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("update"),
            (String) operationObject.get("table"),
            (String) operationObject.get("set"),
            (String) operationObject.get("values"),
            !request.get("2").equals("") ? ", contrasena='" + request.get("2") + "'" : "",
            //"'" + request.get("2").toString() + "'",
            (String) operationObject.get("where"),
            (String) operationObject.get("field1"),
            "'" + request.get("1").toString() + "'"
        };
    }

    /**
     * Creates a SQL query based on the filter form, this method only applies to
     * Usuarios table
     *
     * @param request the JSON request sent from Usuarios frame
     * @return returns the sql query filtered
     */
    private String creatingFilterUsuarios(JSONObject request) {
        String sql = new String();
        if (!request.get("1").equals("")) {
            sql += "usuario LIKE '" + request.get("1") + "%' ";
        }

        if (!request.get("2").equals("")) {
            //whether sql var is empty means there are no condtions yet
            if (sql.equals("")) {
                sql += "telefono LIKE '" + request.get("2") + "%' ";
            } else {
                sql += "AND telefono LIKE '" + request.get("2") + "%' ";
            }
        }

        if (!request.get("3").equals("--Seleccionar--")) {
            //whether sql var is empty means there are no condtions yet
            if (sql.equals("")) {
                sql += "estatus = '" + request.get("3") + "' ";
            } else {
                sql += "AND estatus = '" + request.get("3") + "' ";
            }
        }

        if (!request.get("4").equals("--Seleccionar--")) {
            if (sql.equals("")) {
                sql += "perfil = '" + request.get("4") + "'";
            } else {
                sql += "AND perfil = '" + request.get("4") + "'";
            }
        }
        return sql;
    }

    /**
     * Creates a SQL query based on the filter form, this method only applies to
     * Usuarios table
     *
     * @param request the JSON request sent from Usuarios frame
     * @return returns the sql query filtered
     */
    private String creatingFilterAutos(JSONObject request) {
        String sql = new String();
        if (!request.get("1").equals("--Seleccionar--")) {
            sql += "marca = '" + request.get("1") + "' ";
        }

        if (Double.parseDouble(request.get("2").toString()) > 0) {
            //whether sql var is empty means there are no condtions yet
            if (sql.equals("")) {
                sql += "modelo = " + request.get("2") + " ";
            } else {
                sql += "AND modelo = " + request.get("2") + " ";
            }
        }

        if (!request.get("3").equals("--Seleccionar--")) {
            //whether sql var is empty means there are no condtions yet
            if (sql.equals("")) {
                sql += "color = '" + request.get("3") + "' ";
            } else {
                sql += "AND color = '" + request.get("3") + "' ";
            }
        }

        if (!request.get("4").equals("--Seleccionar--")) {
            if (sql.equals("")) {
                sql += "cambio = '" + request.get("4") + "'";
            } else {
                sql += "AND cambio = '" + request.get("4") + "' ";
            }
        }
        sql += "AND id_usuario != " + request.get("5");
        return sql;
    }

    private static String[] INSERTNEWAUTO(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.INSERTNEWAUTO;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());
        /*
        columns: modelo,imagenes,kilometros,descripcion,marca,cambio,precio,color,estatus,id_usuario
         */
        return new String[]{
            (String) operationObject.get("insert"),
            (String) operationObject.get("table"),
            (String) operationObject.get("columns"),
            "(" + request.get("1") + ", '" + request.get("9") + "'," + request.get("2") + ","
            + "'" + request.get("8") + "','" + request.get("3") + "','" + request.get("5") + "',"
            + "" + request.get("6") + ",'" + request.get("7") + "','ENVENTA'," + request.get("10") + ")"
        };
    }

    private static String[] GETCARS(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.GETCARS;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            request.get("1").toString()
        };
    }

    private static String[] UPDATEAUTO(JSONObject operations, JSONObject request) {
        DATADICS dic = DATADICS.UPDATEAUTO;
        JSONObject operationObject = (JSONObject) operations.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("update"),
            (String) operationObject.get("table"),
            (String) operationObject.get("set"),
            "modelo=" + request.get("1") + ", imagenes='" + request.get("9")
            + "', kilometros=" + request.get("2") + ", descripcion='" + request.get("8") + "'"
            + ", marca='" + request.get("3") + "', cambio='" + request.get("5") + "'"
            + ", precio=" + request.get("6") + ", color='" + request.get("7") + "'",
            (String) operationObject.get("where"),
            "'" + request.get("10").toString() + "'"
        };
    }

    private static String[] SUBMITCOMENTARIO(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.SUBMITCOMENTARIO;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("insert"),
            (String) operationObject.get("table"),
            (String) operationObject.get("columns"),
            "('" + request.get("1") + "', '" + request.get("3") + "'," + request.get("4") + ","
            + "" + request.get("5") + ",'" + request.get("6") + "')"
        };
    }

    private static String[] GETFEEDBACK(JSONObject datadic) {
        DATADICS dic = DATADICS.GETFEEDBACK;
        JSONObject datadicObj = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) datadicObj.get("select"),
            (String) datadicObj.get("table")
        };
    }

    private static String[] GETCATALOGO(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.GETCATALOGO;
        JSONObject datadicObj = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) datadicObj.get("select"),
            (String) datadicObj.get("table"),
            (String) datadicObj.get("where"),
            request.get("1").toString(),
            (String) datadicObj.get("value2")
        };
    }

    private static String[] GETVENDEDOR(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.GETVENDEDOR;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            request.get("1").toString()
        };
    }

    private static String[] GETVENDEDORNAME(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.GETVENDEDORNAME;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            "'" + request.get("1").toString() + "'"
        };
    }

    private static String[] FILTRARAUTOS(JSONObject datadic, JSONObject request) {
        DATADICS dic = DATADICS.FILTRARAUTOS;
        JSONObject operationObject = (JSONObject) datadic.get(dic.getValue());

        String sqlFilter = new Peticiones_test().creatingFilterAutos(request);

        return new String[]{
            (String) operationObject.get("select"),
            (String) operationObject.get("table"),
            (String) operationObject.get("where"),
            sqlFilter
        };
    }

    private String VALIDATEUSERTEST(JSONObject datadic) {
        String sqlQuery = new String();
        DATADICS dic = DATADICS.VALIDATEUSERTEST;
        JSONObject operation = (JSONObject) datadic.get(dic.getValue());
        //sqlQuery = formQuery(sql);
        return sqlQuery;
    }

    public JSONObject createRequestJSON(String datadic, String[] columns, Object[] data) {
        JSONObject requestJSON = new JSONObject();
        HashMap<String, Object> request = new HashMap<>();
        for (int i = 0; i < columns.length; i++) {
            request.put(columns[i], data[i]);
        }
        requestJSON.put("datadic", datadic);
        requestJSON.put("request", request);
        requestJSON.put("response", null);
        return requestJSON;
    }

    public static void main(String[] args) {
        JSONObject requestJSON = new Peticiones_test().createRequestJSON("VALIDATEUSERTEST",
                new String[]{"usuario", "contrasena", "estatus"},
                new Object[]{"admin", "123", "ACTIVO"});

        Peticiones_test peticion = new Peticiones_test(requestJSON);
        peticion.setObjectVO(new SessionVO());
        JSONObject response = peticion.getResult();
    }
}
