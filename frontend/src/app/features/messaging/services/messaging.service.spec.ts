import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MessagingService } from './messaging.service';
import { environment } from '../../../../environments/environment';
import { SenderType } from '../models/message.model';

describe('MessagingService', () => {
  let service: MessagingService;
  let httpMock: HttpTestingController;
  const conversationsUrl = `${environment.apiUrl}/conversations`;
  const messagesUrl = `${environment.apiUrl}/messages`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MessagingService]
    });
    service = TestBed.inject(MessagingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createConversation', () => {
    it('should create a conversation', () => {
      const request = {
        doctorId: 'd1', parentId: 'p1', childId: 'c1',
        subject: 'Test', contextType: 'GENERAL' as any
      };
      const mockResponse = { success: true, data: { id: 'conv-1', ...request } };

      service.createConversation(request).subscribe(response => {
        expect(response.data.id).toBe('conv-1');
      });

      const req = httpMock.expectOne(conversationsUrl);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('getDoctorConversations', () => {
    it('should get conversations for a doctor', () => {
      const mockResponse = { success: true, data: [{ id: 'conv-1', doctorId: 'd1' }] };

      service.getDoctorConversations('d1').subscribe(response => {
        expect(response.data.length).toBe(1);
      });

      const req = httpMock.expectOne(`${conversationsUrl}/doctor/d1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getParentConversations', () => {
    it('should get conversations for a parent', () => {
      const mockResponse = { success: true, data: [{ id: 'conv-1', parentId: 'p1' }] };

      service.getParentConversations('p1').subscribe(response => {
        expect(response.data.length).toBe(1);
      });

      const req = httpMock.expectOne(`${conversationsUrl}/parent/p1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('sendMessage', () => {
    it('should send a message', () => {
      const request = {
        conversationId: 'conv-1',
        senderId: 'p1',
        senderType: SenderType.PARENT,
        content: 'Hello doctor'
      };
      const mockResponse = { success: true, data: { id: 'msg-1', ...request } };

      service.sendMessage(request).subscribe(response => {
        expect(response.data.content).toBe('Hello doctor');
      });

      const req = httpMock.expectOne(messagesUrl);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('getConversationMessages', () => {
    it('should get messages for a conversation', () => {
      const mockResponse = {
        success: true,
        data: [
          { id: 'msg-1', conversationId: 'conv-1', content: 'Hello' },
          { id: 'msg-2', conversationId: 'conv-1', content: 'Hi' }
        ]
      };

      service.getConversationMessages('conv-1').subscribe(response => {
        expect(response.data.length).toBe(2);
      });

      const req = httpMock.expectOne(`${messagesUrl}/conversation/conv-1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('markMessagesAsRead', () => {
    it('should mark messages as read', () => {
      const mockResponse = { success: true, data: null };

      service.markMessagesAsRead('conv-1', 'p1').subscribe(response => {
        expect(response.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${messagesUrl}/conversation/conv-1/mark-read?userId=p1`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  describe('markConversationAsRead', () => {
    it('should mark conversation as read for doctor', () => {
      const mockResponse = { success: true, data: null };

      service.markConversationAsRead('conv-1', true).subscribe(response => {
        expect(response.success).toBeTrue();
      });

      const req = httpMock.expectOne(`${conversationsUrl}/conv-1/mark-read?isDoctor=true`);
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });
});
