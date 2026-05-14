package com.hrms.controller;

import com.hrms.common.Result;
import com.hrms.dto.LoginDTO;
import com.hrms.entity.SysUser;
import com.hrms.mapper.SysUserMapper;
import com.hrms.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
    
    /**
     * 修改密码（需要验证旧密码）
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 新密码长度校验
        if (newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
        return Result.success();
    }

    /**
     * 重置密码（管理员用，不需要旧密码）
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam String username, @RequestParam String password) {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(password));
        sysUserMapper.updateById(user);
        return Result.success("密码重置成功，新密码：" + password);
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        
        // 生成验证码
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = image.createGraphics();
        Random random = new Random();
        
        // 设置背景色
        g.setColor(new Color(240, 244, 250));
        g.fillRect(0, 0, width, height);
        
        // 生成验证码
        String verifyCode = generateVerifyCode(random);
        session.setAttribute("verifyCode", verifyCode);
        
        // 画验证码
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(0, 82, 217));
        g.drawString(verifyCode, 20, 28);
        
        // 干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
        
        g.dispose();
        
        response.setContentType("image/png");
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(image, "png", outputStream);
        outputStream.close();
    }

    private String generateVerifyCode(Random random) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
