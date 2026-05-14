import { request } from '@/utils/request'

// 招聘职位
export interface RecruitPosition {
  id: number; title: string; departmentId: number; departmentName: string
  recruitCount: number; jobType: string; experience: string; education: string
  salaryMin: number | null; salaryMax: number | null; location: string
  status: number; statusName: string; publishTime: string; deadline: string
}

// 候选人
export interface Candidate {
  id: number; positionId: number; positionTitle: string; name: string; gender: string
  phone: string; email: string; age: number | null; education: string; school: string
  major: string; experienceYears: number; currentCompany: string; currentPosition: string
  expectedSalary: number | null; source: string; selfIntro: string; status: number; statusName: string
  remark: string; createTime: string
}

// 面试记录
export interface Interview {
  id: number; candidateId: number; candidateName: string; positionId: number
  round: number; roundName: string; interviewerId: number | null; interviewerName: string
  interviewType: string; interviewDate: string; durationMinutes: number
  location: string; score: number | null; result: string; feedback: string; nextRound: number
}

// 职位API
export const getPositionList = (params: any) => request.get('/recruit/position/list', { params })
export const createPosition = (data: Partial<RecruitPosition>) => request.post('/recruit/position', data)
export const updatePosition = (id: number, data: Partial<RecruitPosition>) => request.put(`/recruit/position/${id}`, data)
export const publishPosition = (id: number) => request.put(`/recruit/position/${id}/publish`)
export const deletePosition = (id: number) => request.delete(`/recruit/position/${id}`)

// 候选人API
export const getCandidateList = (params: any) => request.get('/recruit/candidate/list', { params })
export const createCandidate = (data: Partial<Candidate>) => request.post('/recruit/candidate', data)
export const updateCandidate = (id: number, data: Partial<Candidate>) => request.put(`/recruit/candidate/${id}`, data)
export const updateCandidateStatus = (id: number, data: { status: number; remark?: string }) => request.put(`/recruit/candidate/${id}/status`, data)
export const deleteCandidate = (id: number) => request.delete(`/recruit/candidate/${id}`)

// 面试API
export const getInterviewList = (candidateId?: number) => request.get('/recruit/interview/list', { params: { candidateId } })
export const createInterview = (data: Partial<Interview>) => request.post('/recruit/interview', data)
export const updateInterview = (id: number, data: Partial<Interview>) => request.put(`/recruit/interview/${id}`, data)
export const deleteInterview = (id: number) => request.delete(`/recruit/interview/${id}`)

// 统计
export const getRecruitStats = () => request.get('/recruit/stats')
