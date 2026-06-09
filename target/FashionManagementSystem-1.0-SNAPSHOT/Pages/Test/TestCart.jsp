<%-- 
    Document   : TestCart
    Created on : Jun 8, 2026, 10:09:33 AM
    Author     : Admin
--%>
<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Test Cart</title>
</head>
<body>

<h2>Test Add To Cart</h2>

<form action="${pageContext.request.contextPath}/cart/add"
      method="post">

    Variant ID:
    <input type="text"
           name="variantId">

    <br><br>

    Quantity:
    <input type="number"
           name="quantity"
           value="1">

    <br><br>

    <button type="submit">
        Add To Cart
    </button>

</form>

</body>
</html>