#!/bin/bash

# 定义项目路径
PROJECT_PATH="D:/Projects/PersonalProjects/UserCenter"

# 找出所有使用旧版BaseResponse的Java文件
find_files() {
  grep -l "import com.weiki.usercenterbackend.common.BaseResponse;" $(find "$PROJECT_PATH" -name "*.java") | sort
}

# 替换导入语句
replace_imports() {
  echo "替换以下文件中的BaseResponse导入:"
  
  for file in $(find_files); do
    echo "- $file"
    
    # 使用sed替换导入语句
    sed -i 's/import com.weiki.usercenterbackend.common.BaseResponse;/import com.weiki.usercenterbackend.model.response.BaseResponse;/g' "$file"
    
    # 验证替换是否成功
    if grep -q "import com.weiki.usercenterbackend.model.response.BaseResponse;" "$file"; then
      echo "  √ 替换成功"
    else
      echo "  × 替换失败"
    fi
  done
}

echo "开始处理BaseResponse导入问题..."
replace_imports
echo "处理完成!" 