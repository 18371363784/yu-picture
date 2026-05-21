// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addApproval POST /api/approval/add */
export async function addApprovalUsingPost(
  body: API.PictureApprovalAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/api/approval/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** doApprovalReview POST /api/approval/review */
export async function doApprovalReviewUsingPost(
  body: API.PictureApprovalReviewRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/approval/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listMyApprovals POST /api/approval/list/my */
export async function listMyApprovalsUsingPost(
  body: API.PictureApprovalQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePictureApprovalVO_>('/api/approval/list/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listApprovals POST /api/approval/list */
export async function listApprovalsUsingPost(
  body: API.PictureApprovalQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePictureApprovalVO_>('/api/approval/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
