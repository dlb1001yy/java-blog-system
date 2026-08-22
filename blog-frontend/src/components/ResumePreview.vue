<template>
  <div class="rp-root" :class="'rp-' + style">
    <!-- ============ 现代极简 modern ============ -->
    <div v-if="style === 'modern'" class="rp-modern">
      <header class="m-header">
        <img v-if="resume.avatar" :src="resume.avatar" class="m-avatar" alt="照片" />
        <div>
          <h1>{{ resume.name || '姓名' }}</h1>
          <p class="m-title">{{ resume.jobTitle }}</p>
          <div class="m-meta">
            <span v-if="resume.phone">{{ resume.phone }}</span>
            <span v-if="resume.email">{{ resume.email }}</span>
            <span v-if="resume.address">{{ resume.address }}</span>
            <span v-if="resume.workYears != null">{{ resume.workYears }}年经验</span>
            <span v-if="resume.highestEducation">{{ resume.highestEducation }}</span>
            <span v-if="resume.expectedSalary">{{ resume.expectedSalary }}</span>
          </div>
        </div>
      </header>

      <section v-if="resume.summary">
        <h2>个人简介</h2>
        <p class="rp-text">{{ resume.summary }}</p>
      </section>
      <section v-if="skills.length">
        <h2>技能特长</h2>
        <div class="m-skills">
          <span v-for="s in skills" :key="s.name" class="m-skill-tag">{{ s.name }} · {{ s.level }}</span>
        </div>
      </section>
      <section v-if="works.length">
        <h2>工作经历</h2>
        <div v-for="(w, i) in works" :key="i" class="m-item">
          <div class="m-item-head"><strong>{{ w.company }}</strong><span>{{ w.startDate }} ~ {{ w.endDate || '至今' }}</span></div>
          <p class="m-sub">{{ w.position }}</p>
          <p class="rp-text">{{ w.description }}</p>
        </div>
      </section>
      <section v-if="projects.length">
        <h2>项目经验</h2>
        <div v-for="(p, i) in projects" :key="i" class="m-item">
          <div class="m-item-head"><strong>{{ p.name }}</strong><span>{{ p.date }}</span></div>
          <p class="m-sub">{{ p.role }}<template v-if="p.technologies?.length"> · {{ p.technologies.join(' / ') }}</template></p>
          <p class="rp-text">{{ p.description }}</p>
        </div>
      </section>
      <section v-if="educations.length">
        <h2>教育背景</h2>
        <div v-for="(e, i) in educations" :key="i" class="m-item">
          <div class="m-item-head"><strong>{{ e.school }}</strong><span>{{ e.startDate }} ~ {{ e.endDate }}</span></div>
          <p class="m-sub">{{ e.major }} · {{ e.degree }}</p>
        </div>
      </section>
      <section v-if="certificates.length">
        <h2>证书荣誉</h2>
        <p class="rp-text" v-for="(c, i) in certificates" :key="i">{{ c.name }}<template v-if="c.issuer">（{{ c.issuer }}）</template> {{ c.date }}</p>
      </section>
      <section v-if="resume.selfEvaluation">
        <h2>自我评价</h2>
        <p class="rp-text">{{ resume.selfEvaluation }}</p>
      </section>
      <section v-if="resume.interests">
        <h2>兴趣爱好</h2>
        <p class="rp-text">{{ resume.interests }}</p>
      </section>
    </div>

    <!-- ============ 经典衬线 classic ============ -->
    <div v-else-if="style === 'classic'" class="rp-classic">
      <header class="c-header">
        <h1>{{ resume.name || '姓名' }}</h1>
        <p class="c-title">{{ resume.jobTitle }}</p>
        <p class="c-contact">
          <template v-if="resume.phone">{{ resume.phone }}</template>
          <template v-if="resume.email"> · {{ resume.email }}</template>
          <template v-if="resume.address"> · {{ resume.address }}</template>
        </p>
        <hr class="c-double" />
      </header>
      <section v-if="resume.summary">
        <h3 class="c-center-title">个人简介</h3>
        <p class="rp-text c-center">{{ resume.summary }}</p>
      </section>
      <section v-if="skills.length">
        <h3 class="c-center-title">技能特长</h3>
        <p class="rp-text c-center">{{ skills.map(s => `${s.name}（${s.level}）`).join('　') }}</p>
      </section>
      <section v-if="works.length">
        <h3 class="c-center-title">工作经历</h3>
        <div v-for="(w, i) in works" :key="i" class="c-item">
          <p class="c-line"><strong>{{ w.company }}</strong><span>{{ w.startDate }} ~ {{ w.endDate || '至今' }}</span></p>
          <p class="c-line em">{{ w.position }}</p>
          <p class="rp-text">{{ w.description }}</p>
        </div>
      </section>
      <section v-if="projects.length">
        <h3 class="c-center-title">项目经验</h3>
        <div v-for="(p, i) in projects" :key="i" class="c-item">
          <p class="c-line"><strong>{{ p.name }}</strong><span>{{ p.date }}</span></p>
          <p class="c-line em">{{ p.role }}<template v-if="p.technologies?.length"> · {{ p.technologies.join(' / ') }}</template></p>
          <p class="rp-text">{{ p.description }}</p>
        </div>
      </section>
      <section v-if="educations.length">
        <h3 class="c-center-title">教育背景</h3>
        <div v-for="(e, i) in educations" :key="i" class="c-item">
          <p class="c-line"><strong>{{ e.school }}</strong><span>{{ e.startDate }} ~ {{ e.endDate }}</span></p>
          <p class="c-line em">{{ e.major }} · {{ e.degree }}</p>
        </div>
      </section>
      <section v-if="certificates.length">
        <h3 class="c-center-title">证书荣誉</h3>
        <p class="rp-text" v-for="(c, i) in certificates" :key="i">{{ c.name }}<template v-if="c.issuer">（{{ c.issuer }}）</template> {{ c.date }}</p>
      </section>
      <section v-if="resume.selfEvaluation">
        <h3 class="c-center-title">自我评价</h3>
        <p class="rp-text">{{ resume.selfEvaluation }}</p>
      </section>
    </div>

    <!-- ============ 双栏侧边栏 sidebar ============ -->
    <div v-else-if="style === 'sidebar'" class="rp-sidebar">
      <aside class="s-side">
        <img v-if="resume.avatar" :src="resume.avatar" class="s-avatar" alt="照片" />
        <div v-else class="s-avatar s-avatar-placeholder">{{ (resume.name || '简').charAt(0) }}</div>
        <div class="s-block">
          <h4>联系方式</h4>
          <p v-if="resume.phone">{{ resume.phone }}</p>
          <p v-if="resume.email">{{ resume.email }}</p>
          <p v-if="resume.address">{{ resume.address }}</p>
        </div>
        <div class="s-block" v-if="basics.length">
          <h4>基本信息</h4>
          <p v-for="b in basics" :key="b">{{ b }}</p>
        </div>
        <div class="s-block" v-if="skills.length">
          <h4>技能特长</h4>
          <p v-for="s in skills" :key="s.name">{{ s.name }} · {{ s.level }}</p>
        </div>
        <div class="s-block" v-if="certificates.length">
          <h4>证书荣誉</h4>
          <p v-for="(c, i) in certificates" :key="i">{{ c.name }}</p>
        </div>
        <div class="s-block" v-if="resume.interests">
          <h4>兴趣爱好</h4>
          <p>{{ resume.interests }}</p>
        </div>
      </aside>
      <main class="s-main">
        <h1>{{ resume.name || '姓名' }}</h1>
        <p class="s-title">{{ resume.jobTitle }}</p>
        <section v-if="resume.summary">
          <h3>个人简介</h3>
          <p class="rp-text">{{ resume.summary }}</p>
        </section>
        <section v-if="works.length">
          <h3>工作经历</h3>
          <div v-for="(w, i) in works" :key="i" class="s-item">
            <p class="s-line"><strong>{{ w.company }} · {{ w.position }}</strong><span>{{ w.startDate }} ~ {{ w.endDate || '至今' }}</span></p>
            <p class="rp-text">{{ w.description }}</p>
          </div>
        </section>
        <section v-if="projects.length">
          <h3>项目经验</h3>
          <div v-for="(p, i) in projects" :key="i" class="s-item">
            <p class="s-line"><strong>{{ p.name }} · {{ p.role }}</strong><span>{{ p.date }}</span></p>
            <p v-if="p.technologies?.length" class="s-tech">{{ p.technologies.join(' / ') }}</p>
            <p class="rp-text">{{ p.description }}</p>
          </div>
        </section>
        <section v-if="educations.length">
          <h3>教育背景</h3>
          <div v-for="(e, i) in educations" :key="i" class="s-item">
            <p class="s-line"><strong>{{ e.school }} · {{ e.major }}</strong><span>{{ e.startDate }} ~ {{ e.endDate }}</span></p>
            <p class="rp-text">{{ e.degree }}</p>
          </div>
        </section>
        <section v-if="resume.selfEvaluation">
          <h3>自我评价</h3>
          <p class="rp-text">{{ resume.selfEvaluation }}</p>
        </section>
      </main>
    </div>

    <!-- ============ 粗体页眉 bold ============ -->
    <div v-else class="rp-bold">
      <header class="b-header">
        <div class="b-header-main">
          <h1>{{ resume.name || '姓名' }}</h1>
          <p class="b-title">{{ resume.jobTitle }}</p>
          <div class="b-meta">
            <span v-if="resume.phone">{{ resume.phone }}</span>
            <span v-if="resume.email">{{ resume.email }}</span>
            <span v-if="resume.address">{{ resume.address }}</span>
            <span v-if="resume.workYears != null">{{ resume.workYears }}年经验</span>
            <span v-if="resume.expectedSalary">{{ resume.expectedSalary }}</span>
          </div>
        </div>
        <img v-if="resume.avatar" :src="resume.avatar" class="b-avatar" alt="照片" />
      </header>
      <div class="b-body">
        <section v-if="resume.summary">
          <h2>个人简介</h2>
          <p class="rp-text">{{ resume.summary }}</p>
        </section>
        <section v-if="skills.length">
          <h2>技能特长</h2>
          <div class="b-skills">
            <span v-for="s in skills" :key="s.name" class="b-skill">{{ s.name }} · {{ s.level }}</span>
          </div>
        </section>
        <section v-if="works.length">
          <h2>工作经历</h2>
          <div v-for="(w, i) in works" :key="i" class="b-item">
            <div class="b-item-head"><strong>{{ w.company }} · {{ w.position }}</strong><span>{{ w.startDate }} ~ {{ w.endDate || '至今' }}</span></div>
            <p class="rp-text">{{ w.description }}</p>
          </div>
        </section>
        <section v-if="projects.length">
          <h2>项目经验</h2>
          <div v-for="(p, i) in projects" :key="i" class="b-item">
            <div class="b-item-head"><strong>{{ p.name }} · {{ p.role }}</strong><span>{{ p.date }}</span></div>
            <p v-if="p.technologies?.length" class="b-tech">{{ p.technologies.join(' / ') }}</p>
            <p class="rp-text">{{ p.description }}</p>
          </div>
        </section>
        <section v-if="educations.length">
          <h2>教育背景</h2>
          <div v-for="(e, i) in educations" :key="i" class="b-item">
            <div class="b-item-head"><strong>{{ e.school }} · {{ e.major }} · {{ e.degree }}</strong><span>{{ e.startDate }} ~ {{ e.endDate }}</span></div>
          </div>
        </section>
        <section v-if="certificates.length">
          <h2>证书荣誉</h2>
          <p class="rp-text" v-for="(c, i) in certificates" :key="i">{{ c.name }}<template v-if="c.issuer">（{{ c.issuer }}）</template> {{ c.date }}</p>
        </section>
        <section v-if="resume.selfEvaluation">
          <h2>自我评价</h2>
          <p class="rp-text">{{ resume.selfEvaluation }}</p>
        </section>
        <section v-if="resume.interests">
          <h2>兴趣爱好</h2>
          <p class="rp-text">{{ resume.interests }}</p>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  resume: { type: Object, required: true },
  skills: { type: Array, default: () => [] },
  works: { type: Array, default: () => [] },
  projects: { type: Array, default: () => [] },
  educations: { type: Array, default: () => [] },
  certificates: { type: Array, default: () => [] },
  // modern | classic | sidebar | bold
  style: { type: String, default: 'modern' }
})

const basics = computed(() => {
  const r = props.resume
  const list = []
  if (r.gender === 0 || r.gender === 1) list.push(r.gender === 0 ? '男' : '女')
  if (r.birthDate) list.push(`出生：${r.birthDate}`)
  if (r.maritalStatus != null) list.push(['未婚', '已婚', '离异'][r.maritalStatus] || '')
  if (r.workYears != null) list.push(`${r.workYears}年经验`)
  if (r.highestEducation) list.push(r.highestEducation)
  if (r.hukou) list.push(`户籍：${r.hukou}`)
  return list.filter(Boolean)
})
</script>

<style scoped>
.rp-root { background: #fff; color: #1f2328; line-height: 1.6; }
.rp-text { color: #424a53; white-space: pre-wrap; margin: 4px 0; }
.rp-root section { margin-bottom: 20px; }

/* ---------- 现代极简 modern ---------- */
.rp-modern { padding: 40px 48px; font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif; }
.m-header { display: flex; gap: 20px; align-items: center; margin-bottom: 28px; }
.m-avatar { width: 88px; height: 88px; border-radius: 50%; object-fit: cover; }
.m-header h1 { font-size: 30px; margin: 0; }
.m-title { color: #0f766e; font-weight: 600; margin: 4px 0; }
.m-meta { display: flex; flex-wrap: wrap; gap: 4px 14px; color: #6b7280; font-size: 13px; }
.rp-modern h2 { font-size: 15px; letter-spacing: 1px; color: #0f766e; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; margin: 0 0 10px; }
.m-skills { display: flex; flex-wrap: wrap; gap: 8px; }
.m-skill-tag { background: #f0fdfa; color: #0f766e; border: 1px solid #ccfbf1; border-radius: 999px; padding: 2px 12px; font-size: 13px; }
.m-item { margin-bottom: 14px; }
.m-item-head { display: flex; justify-content: space-between; align-items: baseline; }
.m-item-head span { color: #6b7280; font-size: 13px; }
.m-sub { color: #0f766e; margin: 2px 0; font-size: 14px; }

/* ---------- 经典衬线 classic ---------- */
.rp-classic { padding: 44px 56px; font-family: Georgia, 'Songti SC', 'SimSun', serif; }
.c-header { text-align: center; margin-bottom: 24px; }
.c-header h1 { font-size: 30px; margin: 0 0 6px; letter-spacing: 4px; }
.c-title { margin: 0 0 8px; color: #7c2d12; font-style: italic; }
.c-contact { color: #57534e; font-size: 13px; margin: 0; }
.c-double { border: none; border-top: 3px double #1c1917; margin-top: 12px; }
.c-center-title { text-align: center; letter-spacing: 6px; font-size: 16px; margin: 18px 0 8px; }
.c-item { margin-bottom: 12px; padding: 0 8px; }
.c-line { display: flex; justify-content: space-between; align-items: baseline; margin: 2px 0; }
.c-line span { color: #78716c; font-size: 13px; }
.c-line.em { color: #7c2d12; font-style: italic; }
.c-center { text-align: justify; }

/* ---------- 双栏侧边栏 sidebar ---------- */
.rp-sidebar { display: flex; min-height: 100%; }
.s-side { width: 220px; background: #1e293b; color: #e2e8f0; padding: 28px 20px; flex-shrink: 0; }
.s-avatar { width: 96px; height: 96px; border-radius: 50%; object-fit: cover; margin: 0 auto 18px; display: block; border: 3px solid rgba(255,255,255,.25); }
.s-avatar-placeholder { background: #475569; display: flex; align-items: center; justify-content: center; font-size: 34px; color: #fff; }
.s-block { margin-bottom: 18px; }
.s-block h4 { font-size: 12px; letter-spacing: 2px; color: #94a3b8; border-bottom: 1px solid rgba(255,255,255,.15); padding-bottom: 4px; margin: 0 0 6px; }
.s-block p { margin: 3px 0; font-size: 13px; word-break: break-all; }
.s-main { flex: 1; padding: 32px 36px; }
.s-main h1 { margin: 0; font-size: 28px; }
.s-title { color: #2563eb; font-weight: 600; margin: 4px 0 18px; }
.s-main h3 { font-size: 14px; color: #2563eb; letter-spacing: 1px; border-left: 3px solid #2563eb; padding-left: 8px; margin: 0 0 10px; }
.s-item { margin-bottom: 14px; }
.s-line { display: flex; justify-content: space-between; align-items: baseline; margin: 0; }
.s-line span { color: #6b7280; font-size: 13px; }
.s-tech { color: #2563eb; font-size: 13px; margin: 2px 0; }

/* ---------- 粗体页眉 bold ---------- */
.rp-bold { font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif; }
.b-header { background: #7c2d12; color: #fff; padding: 32px 44px; display: flex; justify-content: space-between; align-items: center; }
.b-header h1 { margin: 0; font-size: 34px; letter-spacing: 2px; }
.b-title { margin: 6px 0 10px; opacity: .9; }
.b-meta { display: flex; flex-wrap: wrap; gap: 4px 16px; font-size: 13px; opacity: .85; }
.b-avatar { width: 96px; height: 96px; border-radius: 12px; object-fit: cover; border: 3px solid rgba(255,255,255,.4); }
.b-body { padding: 28px 44px 40px; }
.b-body h2 { font-size: 16px; color: #7c2d12; margin: 0 0 10px; padding-left: 10px; border-left: 4px solid #7c2d12; }
.b-skills { display: flex; flex-wrap: wrap; gap: 8px; }
.b-skill { background: #fef3ec; color: #7c2d12; border-radius: 4px; padding: 2px 10px; font-size: 13px; }
.b-item { margin-bottom: 14px; }
.b-item-head { display: flex; justify-content: space-between; align-items: baseline; }
.b-item-head span { color: #6b7280; font-size: 13px; }
.b-tech { color: #7c2d12; font-size: 13px; margin: 2px 0; }

@media print {
  .rp-root { box-shadow: none; }
}
</style>
