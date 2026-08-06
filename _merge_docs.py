# -*- coding: utf-8 -*-
import os

src_dir = r'D:\学习资料\study-notes\项目笔记\智谱生成的项目'
out_path = r'd:\my-project\java-blog-system\项目开发文档合集.md'

chapters = [
    ('具体生成代码.md', '# 一、项目架构总览与数据库设计'),
    ('项目操作实操.md', '# 二、开发环境搭建与项目初始化实操'),
    ('项目后端代码.md', '# 三、后端代码：启动类、配置、实体、Mapper、Service、Security'),
    ('项目后端代码补充.md', '# 四、后端代码补充：工具类'),
    ('项目后端问题修改.md', '# 五、后端问题修复'),
    ('项目后端页面.md', '# 六、前台门户代码'),
    ('项目后端管理页面.md', '# 七、管理后台代码'),
    ('项目后端管理页面(补充).md', '# 八、管理后台补充'),
    ('移动端 APP (uni-app) 优化重构版.md', '# 九、移动端 APP 优化重构版'),
    ('移动端APP问题修改.md', '# 十、移动端 APP 问题修复'),
]

intro = """# Java 博客系统项目开发文档合集

> 本文档整合自智谱生成的项目开发过程中的 10 份文档，按架构→实操→后端→前端→后台→移动端的逻辑顺序排列。

---

## 目录

- [一、项目架构总览与数据库设计](#一项目架构总览与数据库设计)
- [二、开发环境搭建与项目初始化实操](#二开发环境搭建与项目初始化实操)
- [三、后端代码：启动类、配置、实体、Mapper、Service、Security](#三后端代码启动类配置实体mapperservicesecurity)
- [四、后端代码补充：工具类](#四后端代码补充工具类)
- [五、后端问题修复](#五后端问题修复)
- [六、前台门户代码](#六前台门户代码)
- [七、管理后台代码](#七管理后台代码)
- [八、管理后台补充](#八管理后台补充)
- [九、移动端 APP 优化重构版](#九移动端-app-优化重构版)
- [十、移动端 APP 问题修复](#十移动端-app-问题修复)
"""


def downgrade_headings(text):
    """将非代码块内的 ATX 标题降一级：# -> ##, ## -> ### ..."""
    lines = text.split('\n')
    in_fence = False
    out = []
    for line in lines:
        stripped = line.lstrip()
        # 检测围栏代码块开闭
        if stripped.startswith('```') or stripped.startswith('~~~'):
            in_fence = not in_fence
            out.append(line)
            continue
        if not in_fence and stripped.startswith('#'):
            i = 0
            while i < len(stripped) and stripped[i] == '#':
                i += 1
            # ATX 标题：1-6 个 # 后跟空格或行尾
            if 1 <= i <= 6 and (i == len(stripped) or stripped[i].isspace()):
                out.append('#' + line)
                continue
        out.append(line)
    return '\n'.join(out)


sections = [intro.rstrip()]

for fname, title in chapters:
    fpath = os.path.join(src_dir, fname)
    with open(fpath, 'r', encoding='utf-8') as f:
        content = f.read()
    processed = downgrade_headings(content).rstrip()
    sections.append(title + '\n\n' + processed)

result = '\n\n---\n\n'.join(sections) + '\n'

with open(out_path, 'w', encoding='utf-8') as f:
    f.write(result)

# 输出统计信息
print('OK ->', out_path)
print('total chars:', len(result))
print('total lines:', result.count('\n') + 1)
for fname, title in chapters:
    fpath = os.path.join(src_dir, fname)
    size = os.path.getsize(fpath)
    print('  ', fname, size, 'bytes')
