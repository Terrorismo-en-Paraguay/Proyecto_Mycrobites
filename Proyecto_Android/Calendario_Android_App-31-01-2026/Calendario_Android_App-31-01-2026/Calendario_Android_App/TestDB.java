import java.sql.*;
public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://vnlsjy.h.filess.io:3305/calendario_app_solutionso";
        String user = "calendario_app_solutionso";
        String pass = "e13dac639f08cb4fed987a36e7b8e30e95cad171";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connection Success!");
            
            String query = "SELECT id_usuario, correo, password_hash, rol FROM usuarios WHERE correo = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "raquel@gmail.com");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("User Found!");
                System.out.println("ID: " + rs.getInt("id_usuario"));
                System.out.println("Email: " + rs.getString("correo"));
                System.out.println("Role: " + rs.getString("rol"));
                String dbHash = rs.getString("password_hash");
                System.out.println("DB Hash: '" + dbHash + "'");
                
                String expectedHash = "jLIjfQZ5yojbZGTqxg2pY0VROWQ=";
                if (expectedHash.equals(dbHash)) {
                    System.out.println("Hash MATCHES!");
                } else {
                    System.out.println("Hash MISMATCH! Expected: " + expectedHash); 
                    if ("12345".equals(dbHash)) {
                        System.out.println("WARNING: DB has plain text password!");
                    } else {
                         // Check for trailing spaces
                         if (dbHash.trim().equals(expectedHash)) {
                             System.out.println("WARNING: DB hash has whitespace issues!");
                         }
                    }
                }
            } else {
                System.out.println("User 'raquel@gmail.com' NOT FOUND in database.");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
