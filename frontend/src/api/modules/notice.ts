import { request } from '@/utils/request'

// ==================== 公告管理接口 ====================

export interface Notice {
  id: number
  title: string
  content: string
  type: number  // 1=普通 2=重要 3=紧急
  status: number  // 0=草稿 1=已发布
  creatorId?: number
  publishTime?: string
  createTime?: string
}

export interface NoticeQuery {
  keyword?: string
  type?: number | undefined
  status?: number | undefined
  page: number
  pageSize: number
}

// 分页查询公告列表
export const getNoticePage = (params: NoticeQuery) => {
  return request.get('/notice/page', { params })
}

// 获取公告详情
export const getNoticeDetail = (id: number) => {
  return request.get<Notice>('/notice/' + id)
}

// 创建公告
export const createNotice = (data: Partial<Notice>) => {
  return request.post('/notice', data)
}

// 更新公告
export const updateNotice = (id: number, data: Partial<Notice>) => {
  return request.put('/notice/' + id, data)
}

// 删除公告
export const deleteNotice = (id: number) => {
  return request.delete('/notice/' + id)
}

// 发布公告
export const publishNotice = (id: number) => {
  return request.post('/notice/publish/' + id)
}
