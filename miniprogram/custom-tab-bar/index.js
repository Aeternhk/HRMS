Component({
  data: {
    value: 'index',
    list: [
      { value: 'index', text: '首页', icon: 'home' },
      { value: 'clock', text: '打卡', icon: 'time' },
      { value: 'vote', text: '投票', icon: 'chat' },
      { value: 'profile', text: '我的', icon: 'user' }
    ]
  },
  methods: {
    onChange(e) {
      const value = e.detail.value;
      this.setData({ value });
      
      const urlMap = {
        'index': '/pages/index/index',
        'clock': '/pages/clock/clock',
        'vote': '/pages/vote/vote',
        'profile': '/pages/profile/profile'
      };
      
      wx.switchTab({
        url: urlMap[value]
      });
    }
  }
});