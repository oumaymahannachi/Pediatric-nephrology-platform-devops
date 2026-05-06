import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Article, Video, QuizQuestion, DailyTip, EducationCategory, CategoryInfo } from './models/education.model';
import { ARTICLES, VIDEOS, QUIZ_QUESTIONS, DAILY_TIPS } from './data/education-content';

@Component({
  selector: 'app-education',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './education.component.html',
  styleUrls: ['./education.component.css']
})
export class EducationComponent implements OnInit {

  activeTab: 'home' | 'articles' | 'videos' | 'quiz' = 'home';
  selectedCategory: EducationCategory | 'all' = 'all';
  selectedArticle: Article | null = null;
  selectedVideo: SafeResourceUrl | null = null;
  searchQuery = '';

  // Quiz state
  currentQuestionIndex = 0;
  selectedAnswer: number | null = null;
  quizAnswers: (number | null)[] = [];
  quizCompleted = false;
  quizScore = 0;
  showExplanation = false;

  // Daily tip
  dailyTip: DailyTip | null = null;

  EducationCategory = EducationCategory;
  readonly QUIZ_QUESTIONS = QUIZ_QUESTIONS;

  categories: CategoryInfo[] = [
    { key: EducationCategory.KIDNEY_DISEASE, label: 'Maladies rénales', icon: '🫘', color: '#6366f1' },
    { key: EducationCategory.MEDICATION, label: 'Médicaments', icon: '💊', color: '#10b981' },
    { key: EducationCategory.NUTRITION, label: 'Alimentation', icon: '🥗', color: '#f59e0b' },
    { key: EducationCategory.MONITORING, label: 'Surveillance', icon: '🩺', color: '#3b82f6' },
    { key: EducationCategory.EMERGENCY, label: 'Urgences', icon: '🚨', color: '#ef4444' }
  ];

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    // Pick daily tip based on day of year
    const dayOfYear = Math.floor((Date.now() - new Date(new Date().getFullYear(), 0, 0).getTime()) / 86400000);
    this.dailyTip = DAILY_TIPS[dayOfYear % DAILY_TIPS.length];
    this.quizAnswers = new Array(QUIZ_QUESTIONS.length).fill(null);
  }

  get filteredArticles(): Article[] {
    return ARTICLES.filter(a => {
      const matchCat = this.selectedCategory === 'all' || a.category === this.selectedCategory;
      const matchSearch = !this.searchQuery || a.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        a.simplifiedTitle.toLowerCase().includes(this.searchQuery.toLowerCase());
      return matchCat && matchSearch;
    });
  }

  get filteredVideos(): Video[] {
    return VIDEOS.filter(v =>
      this.selectedCategory === 'all' || v.category === this.selectedCategory
    );
  }

  get currentQuestion(): QuizQuestion {
    return QUIZ_QUESTIONS[this.currentQuestionIndex];
  }

  getCategoryInfo(cat: EducationCategory): CategoryInfo {
    return this.categories.find(c => c.key === cat) || this.categories[0];
  }

  openArticle(article: Article): void {
    this.selectedArticle = article;
  }

  closeArticle(): void {
    this.selectedArticle = null;
  }

  openVideo(video: Video): void {
    if (video.youtubeId) {
      this.selectedVideo = this.sanitizer.bypassSecurityTrustResourceUrl(
        `https://www.youtube.com/embed/${video.youtubeId}?autoplay=1`
      );
    }
  }

  closeVideo(): void {
    this.selectedVideo = null;
  }

  // Quiz methods
  selectAnswer(index: number): void {
    if (this.showExplanation) return;
    this.selectedAnswer = index;
    this.quizAnswers[this.currentQuestionIndex] = index;
    this.showExplanation = true;
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < QUIZ_QUESTIONS.length - 1) {
      this.currentQuestionIndex++;
      this.selectedAnswer = this.quizAnswers[this.currentQuestionIndex];
      this.showExplanation = this.selectedAnswer !== null;
    } else {
      this.finishQuiz();
    }
  }

  prevQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.selectedAnswer = this.quizAnswers[this.currentQuestionIndex];
      this.showExplanation = this.selectedAnswer !== null;
    }
  }

  finishQuiz(): void {
    this.quizScore = QUIZ_QUESTIONS.filter((q, i) => this.quizAnswers[i] === q.correctIndex).length;
    this.quizCompleted = true;
  }

  restartQuiz(): void {
    this.currentQuestionIndex = 0;
    this.selectedAnswer = null;
    this.quizAnswers = new Array(QUIZ_QUESTIONS.length).fill(null);
    this.quizCompleted = false;
    this.quizScore = 0;
    this.showExplanation = false;
  }

  getScoreMessage(): string {
    const pct = (this.quizScore / QUIZ_QUESTIONS.length) * 100;
    if (pct === 100) return '🏆 Parfait ! Vous êtes un expert !';
    if (pct >= 80) return '🌟 Excellent ! Très bonne connaissance !';
    if (pct >= 60) return '👍 Bien ! Continuez à apprendre !';
    return '📚 Continuez à lire les articles pour progresser !';
  }

  getScoreColor(): string {
    const pct = (this.quizScore / QUIZ_QUESTIONS.length) * 100;
    if (pct >= 80) return '#10b981';
    if (pct >= 60) return '#f59e0b';
    return '#ef4444';
  }

  isAnswerCorrect(index: number): boolean {
    return index === this.currentQuestion.correctIndex;
  }

  isAnswerWrong(index: number): boolean {
    return this.showExplanation && index === this.selectedAnswer && index !== this.currentQuestion.correctIndex;
  }
}
