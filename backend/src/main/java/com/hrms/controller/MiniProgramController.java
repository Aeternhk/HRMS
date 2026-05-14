package com.hrms.controller;

import com.hrms.common.Result;
import com.hrms.entity.*;
import com.hrms.mapper.*;
import com.hrms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 小程序专用接口控制器
 */
@RestController
@RequestMapping("/api/mini")
@CrossOrigin(origins = "*")
public class MiniProgramController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private HrDepartmentMapper hrDepartmentMapper;

    @Autowired
    private HrAttendanceMapper hrAttendanceMapper;

    @Autowired
    private HrLeaveMapper hrLeaveMapper;

    @Autowired
    private HrOvertimeMapper hrOvertimeMapper;

    @Autowired
    private HrSalaryMapper hrSalaryMapper;

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Autowired
    private VoteSubjectMapper voteSubjectMapper;

    @Autowired
    private VoteOptionMapper voteOptionMapper;

    @Autowired
    private VoteRecordMapper voteRecordMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== 1. 登录接口 ====================

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String account = params.get("username");
        String password = params.get("password");

        if (account == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }

        // 支持工号或手机号登录
        SysUser user = sysUserMapper.findByUsername(account);
        if (user == null) {
            user = sysUserMapper.findByPhone(account);
        }

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }

        // 检查登录权限：login_type 必须为 1(仅小程序) 或 3(两者都可)
        Integer loginType = user.getLoginType();
        if (loginType == null || (loginType != 1 && loginType != 3)) {
            return Result.error("该账号无权登录小程序");
        }

        // 获取角色信息
        SysRole role = null;
        if (user.getRoleId() != null) {
            role = sysRoleMapper.selectById(user.getRoleId());
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), "ROLE_" + user.getRoleId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("avatar", user.getAvatar());
        result.put("roleId", user.getRoleId());
        result.put("roleCode", role != null ? role.getCode() : "");
        result.put("roleName", role != null ? role.getName() : "");

        // 添加员工相关信息（直接从user对象获取）
        result.put("employeeNo", user.getUsername());  // username即工号
        result.put("departmentId", user.getDepartmentId());
        result.put("position", user.getPosition());
        result.put("rank", user.getRank());
        result.put("entryDate", user.getEntryDate());
        result.put("gender", user.getGender());
        result.put("phone", user.getPhone());

        // 获取部门名称
        if (user.getDepartmentId() != null) {
            HrDepartment dept = hrDepartmentMapper.selectById(user.getDepartmentId());
            result.put("departmentName", dept != null ? dept.getName() : "");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return Result.success(result);
    }

    // ==================== 2. 公告接口 ====================

    @GetMapping("/notice/list")
    public Result<List<Map<String, Object>>> getNoticeList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "5") int limit) {

        // 获取当月公告
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<SysNotice> notices = sysNoticeMapper.selectByMonth(currentMonth, limit);
        // 当月无数据时fallback到最新5条
        if (notices == null || notices.isEmpty()) {
            notices = sysNoticeMapper.selectLatest(limit);
        }
        List<Map<String, Object>> result = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String[] typeNames = {"", "公告", "通知", "紧急"};
        String[] typeColors = {"", "#0052D9", "#ED7B2F", "#E34D59"};

        for (SysNotice notice : notices) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", notice.getId());
            item.put("title", notice.getTitle());
            item.put("content", notice.getContent());
            item.put("type", notice.getType());
            int t = notice.getType() != null ? notice.getType() : 1;
            item.put("typeName", t < typeNames.length ? typeNames[t] : "公告");
            item.put("typeColor", t < typeColors.length ? typeColors[t] : "#0052D9");
            item.put("publishTime", notice.getPublishTime() != null
                    ? notice.getPublishTime().format(fmt) : "");
            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/notice/detail/{id}")
    public Result<Map<String, Object>> getNoticeDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        SysNotice notice = sysNoticeMapper.selectById(id);
        if (notice == null || notice.getDeleted() == 1) {
            return Result.error("公告不存在");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, Object> result = new HashMap<>();
        result.put("id", notice.getId());
        result.put("title", notice.getTitle());
        result.put("content", notice.getContent());
        result.put("type", notice.getType());
        result.put("publishTime", notice.getPublishTime() != null
                ? notice.getPublishTime().format(fmt) : "");
        return Result.success(result);
    }

    // ==================== 3. 打卡相关接口（支持多时段） ====================

    /**
     * 班次时段定义
     * 支持一天多次打卡：上午、下午、加班
     */
    private static final List<Map<String, String>> SHIFT_DEFINITIONS = new ArrayList<Map<String, String>>() {{
        // 上午班：08:30上班，09:00后迟到，12:00下班，11:30前早退
        Map<String, String> morning = new LinkedHashMap<String, String>();
        morning.put("code", "MORNING");
        morning.put("name", "上午");
        morning.put("workTime", "08:30");
        morning.put("lateThreshold", "09:00");
        morning.put("offTime", "12:00");
        morning.put("earlyThreshold", "11:30");
        add(morning);

        // 下午班：13:30上班，14:00后迟到，17:30下班，17:00前早退
        Map<String, String> afternoon = new LinkedHashMap<String, String>();
        afternoon.put("code", "AFTERNOON");
        afternoon.put("name", "下午");
        afternoon.put("workTime", "13:30");
        afternoon.put("lateThreshold", "14:00");
        afternoon.put("offTime", "17:30");
        afternoon.put("earlyThreshold", "17:00");
        add(afternoon);

        // 加班：18:00开始（灵活时间，无迟到判定），无固定下班
        Map<String, String> overtime = new LinkedHashMap<String, String>();
        overtime.put("code", "OVERTIME");
        overtime.put("name", "加班");
        overtime.put("workTime", "18:00");
        overtime.put("lateThreshold", "");
        overtime.put("offTime", "--:--");
        overtime.put("earlyThreshold", "");
        add(overtime);
    }};

    /** 根据当前时间自动判断属于哪个时段 */
    private String detectCurrentShift(LocalTime time) {
        // 加班：18:00 之后
        if (!time.isBefore(LocalTime.of(18, 0))) return "OVERTIME";
        // 下午：13:00 ~ 17:59
        if (!time.isBefore(LocalTime.of(13, 0)) && time.isBefore(LocalTime.of(18, 0))) return "AFTERNOON";
        // 其他归为上午（含中午休息前）
        return "MORNING";
    }

    /**
     * 获取所有班次配置（多时段规则）
     * 前端用此接口展示班次信息 + 判断当前应打哪个时段
     */
    @GetMapping("/attendance/shift")
    public Result<List<Map<String, Object>>> getShiftInfo(@RequestHeader("Authorization") String token) {
        LocalTime now = LocalTime.now();
        String currentShift = detectCurrentShift(now);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, String> def : SHIFT_DEFINITIONS) {
            Map<String, Object> shift = new HashMap<>(def);
            shift.put("isCurrent", def.get("code").equals(currentShift));
            result.add(shift);
        }

        return Result.success(result);
    }

    /**
     * 获取今日考勤状态（多时段）
     * 返回每个时段的打卡情况
     */
    @GetMapping("/attendance/today")
    public Result<Map<String, Object>> getTodayAttendance(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        LocalDate today = LocalDate.now();

        // 尝试多时段查询（依赖shift_type列），如果表结构未迁移则fallback到旧查询
        List<HrAttendance> records;
        try {
            records = hrAttendanceMapper.findAllByUserIdAndDate(userId, today);
        } catch (Exception e) {
            // 旧表没有shift_type列时使用fallback查询
            records = hrAttendanceMapper.findAllByUserIdAndDateFallback(userId, today);
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        // 构建各时段状态
        List<Map<String, Object>> shiftRecords = new ArrayList<>();
        boolean allDone = true;

        for (Map<String, String> def : SHIFT_DEFINITIONS) {
            String code = def.get("code");
            HrAttendance record = null;
            for (HrAttendance r : records) {
                // 兼容：旧数据shiftType为null时默认归为MORNING
                String rShift = r.getShiftType();
                if (rShift == null || rShift.isEmpty()) rShift = "MORNING";
                if (code.equals(rShift)) { record = r; break; }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("shiftCode", code);
            item.put("shiftName", def.get("name"));
            item.put("workTime", def.get("workTime"));
            item.put("offTime", def.get("offTime"));

            if (record != null) {
                boolean checkedIn = record.getCheckInTime() != null;
                boolean checkedOut = record.getCheckOutTime() != null;

                item.put("checkedIn", checkedIn);
                item.put("checkedOut", checkedOut);
                item.put("checkInTime", checkedIn ? record.getCheckInTime().format(fmt) : null);
                item.put("checkOutTime", checkedOut ? record.getCheckOutTime().format(fmt) : null);

                // 实时判定状态
                String ciStatus = record.getCheckInStatus();
                if ((ciStatus == null || ciStatus.isEmpty()) && record.getCheckInTime() != null) {
                    LocalTime threshold = parseTime(def.get("lateThreshold"));
                    ciStatus = (threshold != null && record.getCheckInTime().toLocalTime().isAfter(threshold)) ? "迟到" : "正常";
                }
                item.put("checkInStatus", ciStatus);

                String coStatus = record.getCheckOutStatus();
                if ((coStatus == null || coStatus.isEmpty()) && record.getCheckOutTime() != null) {
                    LocalTime earlyThreshold = parseTime(def.get("earlyThreshold"));
                    coStatus = (earlyThreshold != null && record.getCheckOutTime().toLocalTime().isBefore(earlyThreshold)) ? "早退" : "正常";
                }
                item.put("checkOutStatus", coStatus);

                if (!(checkedIn && checkedOut)) allDone = false;
            } else {
                item.put("checkedIn", false);
                item.put("checkedOut", false);
                item.put("checkInTime", null);
                item.put("checkOutTime", null);
                item.put("checkInStatus", null);
                item.put("checkOutStatus", null);
                allDone = false;
            }

            shiftRecords.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", shiftRecords);
        result.put("allDone", allDone);

        // 当前应该打的时段（用于按钮显示）
        result.put("currentShift", detectCurrentShift(LocalTime.now()));

        return Result.success(result);
    }

    private LocalTime parseTime(String t) {
        if (t == null || t.isEmpty()) return null;
        try { return LocalTime.parse(t); } catch (Exception e) { return null; }
    }

    /**
     * 打卡接口（支持多时段）
     * 前端传 type: "IN" 或 "OUT"，可选传 shiftType（不传则根据时间自动判断）
     */
    @PostMapping("/clockIn")
    public Result<Map<String, Object>> clockIn(@RequestHeader("Authorization") String token,
                                                @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromToken(token);
        String type = ((String) params.get("type")).toLowerCase();
        String shiftTypeRaw = (String) params.getOrDefault("shiftType", "");

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // 自动判断时段（前端没传或为空时）
        String shiftType;
        if (shiftTypeRaw == null || shiftTypeRaw.trim().isEmpty()) {
            shiftType = detectCurrentShift(currentTime);
        } else {
            shiftType = shiftTypeRaw.toUpperCase();
        }

        // 找到对应的班次定义
        Map<String, String> shiftDef = null;
        for (Map<String, String> def : SHIFT_DEFINITIONS) {
            if (def.get("code").equals(shiftType)) { shiftDef = def; break; }
        }
        if (shiftDef == null) {
            return Result.error("无效的班次类型: " + shiftType);
        }

        // 查询该用户今天该时段的记录
        HrAttendance attendance = hrAttendanceMapper.findByUserIdAndDateAndShift(userId, today, shiftType);

        if (attendance == null) {
            attendance = new HrAttendance();
            attendance.setUserId(userId);
            attendance.setAttendanceDate(today);
            attendance.setShiftType(shiftType);
            attendance.setResult("正常");
            attendance.setCreateTime(now);
        }

        if ("in".equals(type)) {
            if (attendance.getCheckInTime() != null) {
                return Result.error(shiftDef.get("name") + "已上班打卡");
            }
            attendance.setCheckInTime(now);
            // 迟到判定
            LocalTime lateThreshold = parseTime(shiftDef.get("lateThreshold"));
            if (lateThreshold != null && currentTime.isAfter(lateThreshold)) {
                attendance.setCheckInStatus("迟到");
                attendance.setResult("迟到");
            } else {
                attendance.setCheckInStatus("正常");
            }
        } else if ("out".equals(type)) {
            if (attendance.getCheckOutTime() != null) {
                return Result.error(shiftDef.get("name") + "已下班打卡");
            }
            if (attendance.getCheckInTime() == null) {
                return Result.error("请先" + shiftDef.get("name") + "上班打卡");
            }
            attendance.setCheckOutTime(now);
            // 早退判定
            LocalTime earlyThreshold = parseTime(shiftDef.get("earlyThreshold"));
            if (earlyThreshold != null && currentTime.isBefore(earlyThreshold)) {
                attendance.setCheckOutStatus("早退");
                if (!"迟到".equals(attendance.getResult())) {
                    attendance.setResult("早退");
                }
            } else {
                attendance.setCheckOutStatus("正常");
            }
            // 计算工作时长
            long minutes = java.time.Duration.between(attendance.getCheckInTime(), now).toMinutes();
            double hours = minutes / 60.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else {
            return Result.error("无效的打卡类型");
        }

        attendance.setUpdateTime(now);
        hrAttendanceMapper.insertOrUpdate(attendance);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("time", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        result.put("type", type);
        result.put("shiftType", shiftType);
        result.put("shiftName", shiftDef.get("name"));
        result.put("status", "in".equals(type) ? attendance.getCheckInStatus() : attendance.getCheckOutStatus());

        return Result.success(result);
    }

    @GetMapping("/attendance/stats")
    public Result<Map<String, Object>> getMonthAttendanceStats(@RequestHeader("Authorization") String token,
                                                                @RequestParam(required = false) String month) {
        Long userId = getUserIdFromToken(token);

        if (month == null || month.isEmpty()) {
            month = java.time.YearMonth.now().toString();
        }

        Map<String, Object> stats = hrAttendanceMapper.countMonthStats(userId, month);

        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", stats.getOrDefault("totalDays", 0));
        result.put("normalDays", stats.getOrDefault("normalDays", 0));
        result.put("lateCount", stats.getOrDefault("lateCount", 0));
        result.put("absentCount", stats.getOrDefault("absentCount", 0));

        return Result.success(result);
    }

    // ==================== 4. 投票相关接口 ====================

    @GetMapping("/vote/pending-count")
    public Result<Integer> getPendingVoteCount(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        int count = voteSubjectMapper.countPendingVotes(userId);
        return Result.success(count);
    }

    @GetMapping("/vote/list")
    public Result<Map<String, Object>> getVoteList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "ongoing") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Long userId = getUserIdFromToken(token);

        List<VoteSubject> subjects = "ended".equals(status)
                ? voteSubjectMapper.selectEndedList()
                : voteSubjectMapper.selectActiveList();

        String[] typeKeys = {"", "SINGLE", "MULTI", "SCORE", "RANK"};
        String[] typeNames = {"", "单选", "多选", "评分", "排序"};
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Map<String, Object>> list = new ArrayList<>();
        for (VoteSubject subject : subjects) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", subject.getId());
            item.put("title", subject.getTitle());
            item.put("description", subject.getDescription());

            int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;
            item.put("type", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
            item.put("typeName", vt < typeNames.length ? typeNames[vt] : "单选");

            item.put("anonymous", subject.getAnonymous());
            item.put("startTime", subject.getStartTime() != null ? subject.getStartTime().format(fmt) : null);
            item.put("endTime", subject.getEndTime() != null ? subject.getEndTime().format(fmt) : null);
            item.put("status", subject.getStatus());

            int participantCount = voteRecordMapper.countBySubjectId(subject.getId());
            item.put("participantCount", participantCount);

            boolean hasVoted = voteRecordMapper.hasVoted(subject.getId(), userId);
            item.put("hasVoted", hasVoted);

            list.add(item);
        }

        int total = list.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> pageList = list.subList(from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/vote/detail/{id}")
    public Result<Map<String, Object>> getVoteDetail(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        VoteSubject subject = voteSubjectMapper.selectById(id);

        if (subject == null) {
            return Result.error("投票不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", subject.getId());
        result.put("title", subject.getTitle());
        result.put("description", subject.getDescription());

        // voteType：将数据库Integer转为前端期望的String（与列表页保持一致）
        int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;
        String[] typeKeys = {"", "SINGLE", "MULTI", "SCORE", "RANK"};
        String[] typeNames = {"", "单选", "多选", "评分", "排序"};
        result.put("voteType", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
        result.put("type", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");  // 兼容前端用type字段
        result.put("typeName", vt < typeNames.length ? typeNames[vt] : "单选");

        result.put("maxSelections", subject.getMaxSelections());
        result.put("anonymous", subject.getAnonymous());
        result.put("startTime", subject.getStartTime() != null
                ? subject.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        result.put("endTime", subject.getEndTime() != null
                ? subject.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        result.put("status", subject.getStatus());

        List<VoteOption> options = voteOptionMapper.findBySubjectId(id);
        List<Map<String, Object>> optionList = new ArrayList<>();

        for (VoteOption option : options) {
            Map<String, Object> opt = new HashMap<>();
            opt.put("id", option.getId());
            opt.put("content", option.getContent());
            opt.put("sort", option.getSort());
            optionList.add(opt);
        }
        result.put("options", optionList);

        boolean hasVoted = voteRecordMapper.hasVoted(id, userId);
        result.put("hasVoted", hasVoted);

        int participantCount = voteRecordMapper.countBySubjectId(id);
        result.put("participantCount", participantCount);

        return Result.success(result);
    }

    /**
     * 投票结果接口（含各选项票数和百分比）
     * 用于投票结果页展示
     */
    @GetMapping("/vote/result/{id}")
    public Result<Map<String, Object>> getVoteResult(@RequestHeader("Authorization") String token,
                                                     @PathVariable Long id) {
        VoteSubject subject = voteSubjectMapper.selectById(id);

        if (subject == null) {
            return Result.error("投票不存在");
        }

        List<VoteOption> options = voteOptionMapper.findBySubjectId(id);
        int totalVotes = voteRecordMapper.countBySubjectId(id);

        List<Map<String, Object>> optionResults = new ArrayList<>();
        for (VoteOption option : options) {
            Map<String, Object> optResult = new HashMap<>();
            optResult.put("id", option.getId());
            optResult.put("content", option.getContent());
            int voteCount = voteRecordMapper.countByOptionId(option.getId());
            optResult.put("voteCount", voteCount);
            optResult.put("percentage", totalVotes > 0 ? Math.round((float) voteCount / totalVotes * 100) : 0);
            optionResults.add(optResult);
        }

        // 按票数降序排列
        optionResults.sort((a, b) -> (Integer) b.get("voteCount") - (Integer) a.get("voteCount"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", subject.getId());
        result.put("title", subject.getTitle());

        // voteType：将数据库Integer转为前端期望的String
        int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;
        String[] typeKeys = {"", "SINGLE", "MULTI", "SCORE", "RANK"};
        String[] typeNames = {"", "单选", "多选", "评分", "排序"};
        result.put("voteType", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
        result.put("type", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");

        result.put("status", subject.getStatus());
        result.put("totalVotes", totalVotes);
        result.put("participantCount", totalVotes);
        result.put("options", optionResults);

        return Result.success(result);
    }

    /**
     * 提交投票
     * 同时兼容前端字段名 voteId 和后端字段名 subjectId
     */
    @PostMapping("/vote/submit")
    public Result<Void> submitVote(@RequestHeader("Authorization") String token,
                                    @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromToken(token);

        // 兼容前端 voteId 和后端 subjectId 两种字段名
        Object rawSubjectId = params.get("subjectId");
        if (rawSubjectId == null) {
            rawSubjectId = params.get("voteId");
        }
        if (rawSubjectId == null) {
            return Result.error("缺少投票ID");
        }

        Long subjectId = Long.valueOf(rawSubjectId.toString());

        // 检查是否已投票
        if (voteRecordMapper.hasVoted(subjectId, userId)) {
            return Result.error("您已经投过票了");
        }

        // 检查投票是否存在且有效
        VoteSubject subject = voteSubjectMapper.selectById(subjectId);
        if (subject == null || subject.getStatus() != 1) {
            return Result.error("投票不存在或已关闭");
        }

        // 检查是否在投票时间范围内
        LocalDateTime now = LocalDateTime.now();
        if (subject.getStartTime() != null && now.isBefore(subject.getStartTime())) {
            return Result.error("投票尚未开始");
        }
        if (subject.getEndTime() != null && now.isAfter(subject.getEndTime())) {
            return Result.error("投票已截止");
        }

        // 获取选中的选项列表
        @SuppressWarnings("unchecked")
        List<Integer> optionIdList = (List<Integer>) params.get("optionIds");

        if (optionIdList == null || optionIdList.isEmpty()) {
            return Result.error("请选择至少一个选项");
        }

        // 多选题检查最大选择数
        if (subject.getVoteType() != null && subject.getVoteType() == 2 && subject.getMaxSelections() != null) {
            if (optionIdList.size() > subject.getMaxSelections()) {
                return Result.error("最多可选择" + subject.getMaxSelections() + "项");
            }
        }

        StringBuilder optionIds = new StringBuilder();
        for (Integer optId : optionIdList) {
            if (optionIds.length() > 0) optionIds.append(",");
            optionIds.append(optId);
        }

        VoteRecord record = new VoteRecord();
        record.setSubjectId(subjectId);
        record.setVoterId(userId);
        record.setOptionIds(optionIds.toString());
        record.setVoteTime(LocalDateTime.now());

        voteRecordMapper.insert(record);

        return Result.success();
    }

    // ==================== 5. 个人信息接口 ====================

    @GetMapping("/user/profile")
    public Result<Map<String, Object>> getUserProfile(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        result.put("employeeNo", user.getUsername());  // username即工号
        result.put("gender", user.getGender());
        result.put("departmentId", user.getDepartmentId());
        result.put("position", user.getPosition());
        result.put("rank", user.getRank());
        result.put("entryDate", user.getEntryDate());
        result.put("positiveDate", user.getPositiveDate());
        result.put("education", user.getEducation());
        result.put("graduateSchool", user.getGraduateSchool());
        result.put("major", user.getMajor());
        result.put("email", user.getEmail());

        if (user.getDepartmentId() != null) {
            HrDepartment dept = hrDepartmentMapper.selectById(user.getDepartmentId());
            result.put("departmentName", dept != null ? dept.getName() : "");
        }

        return Result.success(result);
    }

    // ==================== 6. 工资条接口 ====================

    @GetMapping("/salary/latest")
    public Result<Map<String, Object>> getLatestSalary(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        HrSalary salary = hrSalaryMapper.findLatestByUserId(userId);

        // 无数据返回成功+空对象（不是错误）
        if (salary == null) {
            return Result.success(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", salary.getId());
        result.put("month", salary.getSalaryMonth());
        result.put("baseSalary", salary.getBaseSalary());
        result.put("positionAllowance", salary.getPositionAllowance());
        result.put("performance", salary.getPerformance());
        result.put("overtimePay", salary.getOvertimePay());
        result.put("bonus", salary.getBonus());
        result.put("mealAllowance", salary.getMealAllowance());
        result.put("transportAllowance", salary.getTransportAllowance());
        result.put("socialInsurance", salary.getSocialInsurance());
        result.put("housingFund", salary.getHousingFund());
        result.put("otherDeduction", salary.getOtherDeduction());
        result.put("netSalary", salary.getNetSalary());
        result.put("status", salary.getStatus());
        result.put("grantDate", salary.getGrantDate());

        return Result.success(result);
    }

    /**
     * 工资列表接口（分页）
     * 用于工资页面展示历史工资条
     */
    @GetMapping("/salary/list")
    public Result<Map<String, Object>> getSalaryList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = getUserIdFromToken(token);
        List<HrSalary> salaries = hrSalaryMapper.findByUserId(userId);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> list = new ArrayList<>();
        for (HrSalary salary : salaries) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", salary.getId());
            item.put("month", salary.getSalaryMonth());
            item.put("baseSalary", salary.getBaseSalary());
            item.put("positionAllowance", salary.getPositionAllowance());
            item.put("performance", salary.getPerformance());
            item.put("overtimePay", salary.getOvertimePay());
            item.put("bonus", salary.getBonus());
            item.put("mealAllowance", salary.getMealAllowance());
            item.put("transportAllowance", salary.getTransportAllowance());
            item.put("socialInsurance", salary.getSocialInsurance());
            item.put("housingFund", salary.getHousingFund());
            item.put("otherDeduction", salary.getOtherDeduction());
            item.put("netSalary", salary.getNetSalary());
            item.put("status", salary.getStatus());
            item.put("grantDate", salary.getGrantDate() != null ? salary.getGrantDate().format(fmt) : null);
            list.add(item);
        }

        // 手动分页
        int total = list.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> pageList = from < total ? list.subList(from, to) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        return Result.success(result);
    }

    // ==================== 7. 请假申请接口 ====================

    @PostMapping("/leave/apply")
    public Result<Void> applyLeave(@RequestHeader("Authorization") String token,
                                    @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromToken(token);

        String leaveType = (String) params.get("type");
        String startDateStr = (String) params.get("startDate");
        String endDateStr = (String) params.get("endDate");
        Double days = Double.valueOf(params.get("days").toString());
        String reason = (String) params.get("reason");

        HrLeave leave = new HrLeave();
        leave.setUserId(userId);
        leave.setLeaveType(leaveType);
        leave.setStartDate(LocalDate.parse(startDateStr));
        leave.setEndDate(LocalDate.parse(endDateStr));
        leave.setDays(BigDecimal.valueOf(days));
        leave.setReason(reason);
        leave.setStatus(0);  // 待审批
        leave.setCreateTime(LocalDateTime.now());
        leave.setUpdateTime(LocalDateTime.now());

        hrLeaveMapper.insert(leave);

        return Result.success();
    }

    @GetMapping("/leave/list")
    public Result<List<Map<String, Object>>> getLeaveList(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<HrLeave> leaves = hrLeaveMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (HrLeave leave : leaves) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", leave.getId());
            item.put("type", leave.getLeaveType());
            item.put("startDate", leave.getStartDate() != null ? leave.getStartDate().format(dateFmt) : null);
            item.put("endDate", leave.getEndDate() != null ? leave.getEndDate().format(dateFmt) : null);
            item.put("days", leave.getDays());
            item.put("reason", leave.getReason());
            // 状态码转为字符串：0=待审批 1=已通过 2=已拒绝
            String[] statusText = {"待审批", "已通过", "已拒绝"};
            int s = leave.getStatus() != null ? leave.getStatus() : 0;
            item.put("status", s < statusText.length ? statusText[s] : "未知");
            item.put("statusCode", s);
            item.put("createTime", leave.getCreateTime() != null ? leave.getCreateTime().format(dateTimeFmt) : null);
            result.add(item);
        }

        return Result.success(result);
    }

    // ==================== 8. 加班申请接口 ====================

    @PostMapping("/overtime/apply")
    public Result<Void> applyOvertime(@RequestHeader("Authorization") String token,
                                       @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromToken(token);

        String overtimeDateStr = params.get("overtimeDate") != null ? (String) params.get("overtimeDate") : (String) params.get("date");
        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        Double hours = Double.valueOf(params.get("hours").toString());
        String reason = (String) params.get("reason");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        HrOvertime overtime = new HrOvertime();
        overtime.setUserId(userId);
        overtime.setOvertimeDate(LocalDate.parse(overtimeDateStr));
        overtime.setStartTime(LocalDateTime.parse(overtimeDateStr + " " + startTimeStr, dateTimeFormatter));
        overtime.setEndTime(LocalDateTime.parse(overtimeDateStr + " " + endTimeStr, dateTimeFormatter));
        overtime.setHours(BigDecimal.valueOf(hours));
        overtime.setReason(reason);
        overtime.setStatus(0);  // 待审批
        overtime.setCreateTime(LocalDateTime.now());
        overtime.setUpdateTime(LocalDateTime.now());

        hrOvertimeMapper.insert(overtime);

        return Result.success();
    }

    @GetMapping("/overtime/list")
    public Result<List<Map<String, Object>>> getOvertimeList(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<HrOvertime> overtimes = hrOvertimeMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (HrOvertime overtime : overtimes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", overtime.getId());
            item.put("overtimeDate", overtime.getOvertimeDate() != null ? overtime.getOvertimeDate().format(dateFmt) : null);
            item.put("startTime", overtime.getStartTime() != null ? overtime.getStartTime().format(dateTimeFmt) : null);
            item.put("endTime", overtime.getEndTime() != null ? overtime.getEndTime().format(dateTimeFmt) : null);
            item.put("hours", overtime.getHours());
            item.put("reason", overtime.getReason());
            // 状态码转为字符串
            String[] statusText = {"待审批", "已通过", "已拒绝"};
            int s = overtime.getStatus() != null ? overtime.getStatus() : 0;
            item.put("status", s < statusText.length ? statusText[s] : "未知");
            item.put("statusCode", s);
            item.put("createTime", overtime.getCreateTime() != null ? overtime.getCreateTime().format(dateTimeFmt) : null);
            result.add(item);
        }

        return Result.success(result);
    }

    // ==================== 9. 申请统计接口（我的页面用） ====================

    /**
     * 获取用户的申请统计（请假+加班合并列表）
     * 用于"我的"页面展示待审批/已通过/未通过的申请数量
     */
    @GetMapping("/application/list")
    public Result<Map<String, Object>> getApplicationList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Long userId = getUserIdFromToken(token);

        // 合并请假和加班记录
        List<HrLeave> leaves = hrLeaveMapper.findByUserId(userId);
        List<HrOvertime> overtimes = hrOvertimeMapper.findByUserId(userId);

        List<Map<String, Object>> allApps = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 添加请假记录
        for (HrLeave leave : leaves) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", leave.getId());
            item.put("type", "LEAVE");
            item.put("typeName", "请假");
            item.put("title", leave.getLeaveType());
            item.put("date", leave.getStartDate() != null ? leave.getStartDate().toString() : "");
            item.put("status", leave.getStatus());
            // 状态映射
            if (leave.getStatus() == 0) item.put("statusText", "PENDING");
            else if (leave.getStatus() == 1) item.put("statusText", "APPROVED");
            else item.put("statusText", "REJECTED");
            item.put("createTime", leave.getCreateTime() != null ? leave.getCreateTime().format(fmt) : "");
            allApps.add(item);
        }

        // 添加加班记录
        for (HrOvertime ot : overtimes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ot.getId());
            item.put("type", "OVERTIME");
            item.put("typeName", "加班");
            item.put("title", "加班申请");
            item.put("date", ot.getOvertimeDate() != null ? ot.getOvertimeDate().toString() : "");
            item.put("status", ot.getStatus());
            if (ot.getStatus() == 0) item.put("statusText", "PENDING");
            else if (ot.getStatus() == 1) item.put("statusText", "APPROVED");
            else item.put("statusText", "REJECTED");
            item.put("createTime", ot.getCreateTime() != null ? ot.getCreateTime().format(fmt) : "");
            allApps.add(item);
        }

        // 按创建时间倒序排列
        allApps.sort((a, b) -> {
            String ta = (String) a.get("createTime");
            String tb = (String) b.get("createTime");
            return tb.compareTo(ta);
        });

        // 手动分页
        int total = allApps.size();
        int from = Math.min((page - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> pageList = from < total ? allApps.subList(from, to) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        return Result.success(result);
    }

    // ==================== 10. 修改密码接口 ====================

    @PostMapping("/user/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, String> params) {
        Long userId = getUserIdFromToken(token);
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        if (newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);

        return Result.success();
    }

    // ==================== 11. 修改手机号接口 ====================

    /**
     * 修改手机号（仅修改phone字段，username=工号保持不变）
     * POST /api/mini/user/change-phone
     */
    @PostMapping("/user/change-phone")
    public Result<Void> changePhone(@RequestHeader("Authorization") String token,
                                     @RequestBody Map<String, String> params) {
        Long userId = getUserIdFromToken(token);
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        String newPhone = params.get("newPhone");
        String verifyCode = params.get("verifyCode");

        if (newPhone == null || verifyCode == null) {
            return Result.error("参数不完整");
        }

        // 校验手机号格式
        if (!newPhone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确");
        }

        // 检查新手机号是否已被占用（排除自己）
        SysUser existingUser = sysUserMapper.findByPhone(newPhone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            return Result.error("该手机号已被其他账号绑定");
        }

        // TODO: 校验验证码（需接入短信服务）
        // if (!smsService.verifyCode(newPhone, verifyCode)) {
        //     return Result.error("验证码错误或已过期");
        // }

        // 仅修改 phone 字段，username（工号）保持不变
        user.setPhone(newPhone);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return Result.success();
    }

    // ==================== 辅助方法 ====================

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
