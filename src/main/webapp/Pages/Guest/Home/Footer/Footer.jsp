<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<style>
    .yody-footer {
        background-color: #0b1426;
        color: #ffffff;
        padding: 50px 20px 30px;
        font-family: 'Inter', 'Space Grotesk', sans-serif;
        font-size: 0.9rem;
    }
    .yody-footer a {
        color: #ffffff;
        text-decoration: none;
    }
    .yody-footer a:hover {
        color: #fdb913;
    }
    
    .footer-top {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        border-bottom: 1px solid #1f2a40;
        padding-bottom: 30px;
        margin-bottom: 30px;
        gap: 30px;
    }
    .footer-col-subscribe {
        flex: 1.5;
        min-width: 320px;
    }
    .footer-col-subscribe h4 {
        font-size: 1.1rem;
        font-weight: 700;
        margin-bottom: 12px;
        text-transform: uppercase;
    }
    .footer-col-subscribe p {
        color: #a0a4ab;
        margin-bottom: 15px;
        line-height: 1.5;
        font-size: 0.85rem;
        max-width: 90%;
    }
    .subscribe-form {
        display: flex;
        gap: 12px;
    }
    .subscribe-form input {
        flex: 1;
        padding: 12px 20px;
        border-radius: 999px;
        border: none;
        outline: none;
        max-width: 250px;
        font-size: 0.85rem;
    }
    .subscribe-form button {
        padding: 12px 30px;
        border-radius: 999px;
        border: none;
        background-color: #fdb913;
        color: #000;
        font-weight: 700;
        cursor: pointer;
        transition: opacity 0.2s;
    }
    .subscribe-form button:hover {
        opacity: 0.9;
    }
    .footer-col-contact {
        display: flex;
        gap: 40px;
        flex: 2;
        min-width: 320px;
    }
    .contact-item {
        display: flex;
        gap: 12px;
        align-items: flex-start;
    }
    .contact-item .material-symbols-outlined {
        font-size: 26px;
        margin-top: 2px;
    }
    .footer-col-social {
        display: flex;
        gap: 12px;
        align-items: flex-start;
    }
    .social-btn {
        width: 40px;
        height: 40px;
        border: 1px solid #ffffff;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s;
        color: #ffffff; 
    }
    .social-btn:hover {
        background-color: #ffffff;
        color: #0b1426;
    }
    
    .footer-bottom {
        color: #a0a4ab;
        font-size: 0.8rem;
        line-height: 1.6;
    }
    .footer-bottom strong {
        color: #ffffff;
        font-size: 0.9rem;
        display: block;
        margin: 15px 0 8px;
    }
    .footer-bottom p {
        margin-bottom: 5px;
    }
</style>

<footer class="yody-footer">
    <div class="container-fluid" style="max-width: 1300px; margin: 0 auto; padding: 0 20px;">
        
        <div class="footer-top">
            <div class="footer-col-subscribe">
                <h4>Welcome FASHION STORE</h4>
                <p>We deeply value and welcome all customer feedback, aiming to continuously improve and elevate both the service experience and product quality.</p>
                <form class="subscribe-form" onsubmit="event.preventDefault();">
                    <input type="email" placeholder="Enter your email address" required>
                    <button type="submit">Send</button>
                </form>
            </div>

            <div class="footer-col-contact">
                <div class="contact-item">
                    <span class="material-symbols-outlined">call</span>
                    <div>
                        <div style="color: #a0a4ab; margin-bottom: 5px; font-size: 0.85rem;">Hotline</div>
                        <div style="font-size: 1.25rem; font-weight: 700;">1800 2086</div>
                        <div style="color: #a0a4ab; font-size: 0.8rem; margin-top: 8px; line-height: 1.5;"></div>
                    </div>
                </div>
                <div class="contact-item">
                    <span class="material-symbols-outlined">mail</span>
                    <div>
                        <div style="color: #a0a4ab; margin-bottom: 5px; font-size: 0.85rem;">Email</div>
                        <div style="font-size: 0.95rem;">chamsockhachhang@fashionstore.com</div>
                    </div>
                </div>
            </div>

            <div class="footer-col-social">
                <a href="#" class="social-btn" title="Zalo"><span class="material-symbols-outlined">forum</span></a>
                <a href="#" class="social-btn" title="Messenger"><span class="material-symbols-outlined">chat</span></a>
                
                <!-- ĐÃ THAY ĐỔI ICON TIKTOK BẰNG SVG CHUẨN -->
                <a href="#" class="social-btn" title="Tiktok">
                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                        <path d="M9 0h1.98c.144.715.54 1.617 1.235 2.512C12.895 3.389 13.797 4 15 4v2c-1.753 0-3.07-.814-4-1.829V11a5 5 0 1 1-5-5v2a3 3 0 1 0 3 3V0Z"/>
                    </svg>
                </a>
                
                <a href="#" class="social-btn" title="Youtube"><span class="material-symbols-outlined">play_arrow</span></a>
                
                <a href="#" class="social-btn" title="Instagram">
                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                      <path d="M8 0C5.829 0 5.556.01 4.703.048 3.85.088 3.269.222 2.76.42a3.917 3.917 0 0 0-1.417.923A3.927 3.927 0 0 0 .42 2.76C.222 3.268.087 3.85.048 4.7.01 5.555 0 5.827 0 8.001c0 2.172.01 2.444.048 3.297.04.852.174 1.433.372 1.942.205.526.478.972.923 1.417.444.445.89.719 1.416.923.51.198 1.09.333 1.942.372C5.555 15.99 5.827 16 8 16s2.444-.01 3.298-.048c.851-.04 1.434-.174 1.943-.372a3.916 3.916 0 0 0 1.416-.923c.445-.445.718-.891.923-1.417.197-.509.332-1.09.372-1.942C15.99 10.445 16 10.173 16 8s-.01-2.445-.048-3.299c-.04-.851-.175-1.433-.372-1.941a3.926 3.926 0 0 0-.923-1.417A3.911 3.911 0 0 0 13.24.42c-.51-.198-1.092-.333-1.943-.372C10.443.01 10.172 0 7.998 0h.003zm-.717 1.442h.718c2.136 0 2.389.007 3.232.046.78.035 1.204.166 1.486.275.373.145.64.319.92.599.28.28.453.546.598.92.11.281.24.705.275 1.485.039.843.047 1.096.047 3.231s-.008 2.389-.047 3.232c-.035.78-.166 1.203-.275 1.485a2.47 2.47 0 0 1-.599.919c-.28.28-.546.453-.92.598-.28.11-.704.24-1.485.276-.843.038-1.096.047-3.232.047s-2.39-.009-3.233-.047c-.78-.036-1.203-.166-1.485-.276a2.478 2.478 0 0 1-.92-.598 2.48 2.48 0 0 1-.6-.92c-.109-.281-.24-.705-.275-1.485-.038-.843-.046-1.096-.046-3.233 0-2.136.008-2.388.046-3.231.036-.78.166-1.204.276-1.486.145-.373.319-.64.599-.92.28-.28.546-.453.92-.598.282-.11.705-.24 1.485-.276.738-.034 1.024-.044 2.515-.045v.002zm4.988 1.328a.96.96 0 1 0 0 1.92.96.96 0 0 0 0-1.92zm-4.27 1.122a4.109 4.109 0 1 0 0 8.217 4.109 4.109 0 0 0 0-8.217zm0 1.441a2.667 2.667 0 1 1 0 5.334 2.667 2.667 0 0 1 0-5.334z"/>
                    </svg>
                </a>
            </div>
        </div>

        <div class="footer-bottom">
            <p style="margin-bottom: 15px;">This website is protected by Google's reCAPTCHA. <a href="#" style="text-decoration: underline; color:#a0a4ab;">Privacy Policy</a> And <a href="#" style="text-decoration: underline; color:#a0a4ab;">Terms of Service</a> Google's apply.</p>
            
            <strong>@ FASHION STORE FASHION JOINT STOCK COMPANY</strong>
            <p>Enterprise Registration Certificate No. 0801206940, issued by the Business Registration and Enterprise Management Division – Department of Planning and Investment on March 4, 2026.</p>
            <p>Address: Nguyen Van Cu Street, An Khanh Ward, Ninh Kieu District, Can Tho City.</p>
            <p>Legal representative: Mr. Phan Tran Hieu Nhan – General Director</p>
        </div>
        
    </div>
</footer>