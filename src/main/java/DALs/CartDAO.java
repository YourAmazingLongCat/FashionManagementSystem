/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;

/**
 *
 * @author Admin
 */
import Models.Cart;
import Utils.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CartDAO extends DBContext {

    public Cart getActiveCart(String accountId) {

        String sql =
                "SELECT * FROM Carts "
                + "WHERE accountId=? "
                + "AND status='Active'";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ps.setString(1, accountId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Cart(
                        rs.getString("cartId"),
                        rs.getString("accountId"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createCart(String accountId) {

        String cartId =
                "CART" + System.currentTimeMillis();

        String sql =
                "INSERT INTO Carts VALUES(?,?,?)";

        try {

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ps.setString(1, cartId);
            ps.setString(2, accountId);
            ps.setString(3, "Active");

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cartId;
    }
}