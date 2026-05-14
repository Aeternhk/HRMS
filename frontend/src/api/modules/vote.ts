import { request } from '@/utils/request'

// ==================== 投票相关接口 ====================

export interface Vote {
  id: number
  title: string
  description: string
  type: number  // 1:单选 2:多选 3:评分 4:排序
  isAnonymous: boolean
  startTime: string
  endTime: string
  status: number  // 0:未开始 1:进行中 2:已结束
  totalVotes: number
  totalParticipants: number
  allowRevote: boolean
  showResultBeforeEnd: boolean
  createBy: number
  createName: string
  createTime: string
}

export interface VoteOption {
  id: number
  voteId: number
  content: string
  description?: string
  imageUrl?: string
  sort: number
  voteCount: number
  votePercentage: number
}

export interface VoteDetail extends Vote {
  options: VoteOption[]
  hasVoted: boolean
  userVoteOptions?: number[]
  userScore?: number
}

export interface VoteQuery {
  keyword?: string
  type?: number
  status?: number
  page: number
  pageSize: number
}

// 获取投票分页列表
export const getVotePage = (params: VoteQuery) => {
  return request.get('/vote/page', { params })
}

// 获取投票详情
export const getVoteDetail = (id: number) => {
  return request.get<VoteDetail>('/vote/' + id)
}

// 创建投票
export const createVote = (data: any) => {
  return request.post('/vote', data)
}

// 更新投票
export const updateVote = (data: any) => {
  return request.put('/vote', data)
}

// 删除投票
export const deleteVote = (id: number) => {
  return request.delete('/vote/' + id)
}

// 投票
export const submitVote = (voteId: number, optionIds: number[], score?: number) => {
  return request.post('/vote/submit', { voteId, optionIds, score })
}

// 获取投票结果
export const getVoteResult = (id: number) => {
  return request.get<VoteDetail>('/vote/result/' + id)
}

// 结束投票
export const endVote = (id: number) => {
  return request.post('/vote/end/' + id)
}

// 我的投票记录
export const getMyVotes = () => {
  return request.get<Vote[]>('/vote/my')
}
