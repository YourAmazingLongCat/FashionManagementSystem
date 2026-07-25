
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DALs;

import Models.Voucher;
import Utils.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    public Voucher getVoucherByCode(String voucherCode) {
        String query = "SELECT * FROM Vouchers WHERE voucherCode = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, voucherCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoucher(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.getVoucherByCode: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String checkVoucherValidity(Voucher v, double cartTotal) {
        if (v == null) {
            return "Mã giảm giá không tồn tại!";
        }

        if (!"Active".equalsIgnoreCase(v.getStatus())) {
            return "Mã giảm giá này đã bị vô hiệu hóa hoặc hết hạn.";
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime < v.getStartDate().getTime()) {
            return "Mã giảm giá này chưa đến thời gian áp dụng.";
        }
        if (currentTime > v.getEndDate().getTime()) {
            return "Mã giảm giá này đã hết hạn sử dụng.";
        }

        if (v.getUsedCount() >= v.getUsageLimit()) {
            return "Mã giảm giá này đã hết lượt sử dụng.";
        }

        if (cartTotal < v.getMinOrderValue()) {
            return "Đơn hàng chưa đạt giá trị tối thiểu (" + String.format("%,.0f", v.getMinOrderValue()) + "đ) để dùng mã này.";
        }

        return "VALID";
    }

    private Voucher mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        // Dùng trim() để gọt sạch khoảng trắng thừa nếu DB lưu kiểu CHAR/NCHAR
        v.setVoucherId(rs.getString("voucherId") != null ? rs.getString("voucherId").trim() : null);
        v.setVoucherCode(rs.getString("voucherCode") != null ? rs.getString("voucherCode").trim() : null);
        v.setDiscountType(rs.getString("discountType") != null ? rs.getString("discountType").trim() : null);
        v.setDiscountValue(rs.getDouble("discountValue"));
        v.setMinOrderValue(rs.getDouble("minOrderValue"));

        double maxDisc = rs.getDouble("maxDiscount");
        v.setMaxDiscount(rs.wasNull() ? null : maxDisc);

        v.setStartDate(rs.getTimestamp("startDate"));
        v.setEndDate(rs.getTimestamp("endDate"));
        v.setUsageLimit(rs.getInt("usageLimit"));
        v.setUsedCount(rs.getInt("usedCount"));
        v.setStatus(rs.getString("status") != null ? rs.getString("status").trim() : null);
        v.setCreatedAt(rs.getTimestamp("createdAt"));
        return v;
    }

    public List<Voucher> getAllVouchers() {
        List<Voucher> list = new ArrayList<>();
        String query = "SELECT * FROM Vouchers ORDER BY createdAt DESC";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToVoucher(rs));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.getAllVouchers: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Voucher getVoucherById(String voucherId) {
        String query = "SELECT * FROM Vouchers WHERE voucherId = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoucher(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.getVoucherById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertVoucher(Voucher v) {
        String query = "INSERT INTO Vouchers (voucherId, voucherCode, discountType, discountValue, minOrderValue, maxDiscount, startDate, endDate, usageLimit, usedCount, status, createdAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, GETDATE())";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, v.getVoucherId());
            ps.setString(2, v.getVoucherCode());
            ps.setString(3, v.getDiscountType());
            ps.setDouble(4, v.getDiscountValue());
            ps.setDouble(5, v.getMinOrderValue());

            if (v.getMaxDiscount() != null) {
                ps.setDouble(6, v.getMaxDiscount());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }

            ps.setTimestamp(7, v.getStartDate());
            ps.setTimestamp(8, v.getEndDate());
            ps.setInt(9, v.getUsageLimit());
            ps.setString(10, v.getStatus() != null ? v.getStatus() : "Active");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.insertVoucher: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateVoucher(Voucher v) {
        String query = "UPDATE Vouchers SET voucherCode = ?, discountType = ?, discountValue = ?, minOrderValue = ?, "
                + "maxDiscount = ?, startDate = ?, endDate = ?, usageLimit = ?, status = ? "
                + "WHERE voucherId = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, v.getVoucherCode());
            ps.setString(2, v.getDiscountType());
            ps.setDouble(3, v.getDiscountValue());
            ps.setDouble(4, v.getMinOrderValue());

            if (v.getMaxDiscount() != null) {
                ps.setDouble(5, v.getMaxDiscount());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }

            ps.setTimestamp(6, v.getStartDate());
            ps.setTimestamp(7, v.getEndDate());
            ps.setInt(8, v.getUsageLimit());
            ps.setString(9, v.getStatus());
            ps.setString(10, v.getVoucherId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.updateVoucher: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ĐÃ SỬA: Quét cả voucherId lẫn voucherCode để chắc chắn 100% tìm thấy dữ liệu
    public boolean toggleVoucherStatus(String identifier, String newStatus) {
        String query = "UPDATE Vouchers SET status = ? WHERE voucherId = ? OR voucherCode = ?";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newStatus);
            ps.setString(2, identifier);
            ps.setString(3, identifier);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại VoucherDAO.toggleVoucherStatus: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

