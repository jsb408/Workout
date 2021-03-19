# Workout

운동을 위한 타이머 앱

- 진행기간 : 2020. 07. 23 ~ 2020. 09. 06
- 사용기술 : Android Studio, Kotlin, Realm, MPAndroidchart

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629088-427b8400-8834-11eb-896c-fdf5dfdf84b1.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111629114-460f0b00-8834-11eb-94b8-1e520ad54b45.jpg" width="30%"/></p>

## 서비스 소개

- Workout은 운동하는 분들을 위한 타이머 앱입니다.
- 세트, 라운드 수를 설정하여 원하는 시간만큼 타이머를 작동시킬 수 있습니다.
- 카운트업, 카운트다운 타이머를 모두 제공해 원하는 방식으로 작동할 수 있습니다.
- 운동을 끝마치면 로컬DB에 저장돼 그래프 형태로 운동 시간을 파악할 수 있습니다.
- 수면 사이클을 고려한 적절한 취침시간, 기상시간을 제공합니다.

## 상세 기능 소개

### 1. 타이머

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629351-8c646a00-8834-11eb-8da4-ce7b060d5854.jpg" width="30%"/></p>

- 상황에 따라 다양한 타이머를 제공합니다.
- 타이머 영역을 탭하여 타이머를 작동할 수 있습니다.

### 1-1. 대기 시간

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629415-9be3b300-8834-11eb-876d-b8d275d22909.jpg" width="30%"/></p>

- 타이머 부분을 탭하면 5초 간 대기시간이 지난 후 운동 타이머가 시작됩니다.

### 1-2. 운동 타이머

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629506-b7e75480-8834-11eb-817e-9084b8598623.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111629561-c59cda00-8834-11eb-86f7-92f39388359c.jpg" width="30%"/></p>

- 운동 타이머가 시작되면 타이머 유형에 따라 시간이 증가 또는 감소합니다.
- 하단에는 휴식 시간이 표시됩니다.
- 타이머를 탭하면 일시정지가 됩니다.
- 일시 정지 상태에서 다시 탭하면 타이머가 다시 동작하며 두번 탭할 시 휴식 타이머로 넘어갑니다.
- 카운트다운 타이머일 경우 타이머가 0이 되면 휴식 타이머로 넘어갑니다.

### 1-3. 휴식 타이머

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629702-ebc27a00-8834-11eb-9768-9f539e989a4d.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111629718-f11fc480-8834-11eb-821b-afaa1f511526.jpg" width="30%"/></p>

- 휴식 타이머는 설정된 시간에서 점점 감소합니다.
- 하단에는 운동 시간이 표시됩니다.
- 타이머를 탭하면 일시정지가 됩니다.
- 일시 정지 상태에서 다시 탭하면 타이머가 다시 동작하며 두 번 탭할 시 운동 타이머로 넘어갑니다.
- 타이머가 종료되면 운동 타이머로 넘어갑니다.
- 마지막 세트의 휴식 타이머일 경우 운동 타이머 대신 인터벌 타이머가 동작합니다.
- 휴식 타이머가 종료되면 현재 운동 로그에 기록됩니다.

### 1-4. 인터벌 타이머

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629820-0bf23900-8835-11eb-9eb9-f3d5013e87ae.jpg" width="30%"/></p>

- 인터벌 타이머는 설정된 시간에서 점점 감소합니다.
- 하단에는 운동 시간이 표시됩니다.
- 두번 탭 할 경우 넘어갈 수 있습니다.

### 1-5. 운동 종료

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629924-23c9bd00-8835-11eb-8283-4eed66598351.jpg" width="30%"/></p>

- 설정된 모든 세트와 모든 라운드가 종료되면 운동이 종료됩니다.
- 총 세트 수, 총 라운드 수와 총 운동 시간이 표시됩니다.
- 운동이 종료되면 해당 기록이 Realm에 저장됩니다.

### 1-6. 현재 운동 로그

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111629981-33e19c80-8835-11eb-866c-e8ac96f42c9a.jpg" width="30%"/></p>

- 현재 운동중인 시간 기록이 표시됩니다.
- 휴식 시간이 끝난 세트만 표시됩니다.
- 해당 기록은 모든 운동이 종료된 후 Realm에 저장됩니다.

### 1-7. 타이머 설정

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111630065-4eb41100-8835-11eb-86f5-b2129369475f.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111630089-55428880-8835-11eb-9f7a-798c0a37ab31.jpg" width="30%"/></p>

- 각 CardView를 탭하면 해당 사항을 변경할 수 있습니다.
- 모든 항목은 Spinner가 표시되는 Dialog를 통해 변경됩니다.
- Workout Timer Type을 Count up으로 설정할 경우 Workout Time 설정은 비활성화됩니다.
- 최상단에는 총 예상 운동 시간이 표시됩니다.
- 운동 중 타이머 설정을 변경할 경우 다음 타이머부터 적용됩니다.

### 2. 운동 기록

- Realm에 저장된 운동 기록을 표시합니다.

### 2-1. RECORD

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111630198-72775700-8835-11eb-9a09-492a4144e8fc.jpg" width="30%"/></p>

- cardView 형태로 운동 기록을 표시합니다.
- 운동 시각, 총 운동 시간, 운동 시간, 휴식 시간, 세트 수, 라운드 수를 표시합니다.

### 2-2. STATS

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111630314-933fac80-8835-11eb-96b6-2b31ccb3d431.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111630326-976bca00-8835-11eb-8d17-1ea90c266dd2.jpg" width="30%"/></p>

- chart 형태로 운동 기록을 표시합니다.
- 막대를 탭하면 하단에 상세 정보가 cardView로 표시됩니다.

### 3. 잠 들 시간

<p align="center"><img src="https://user-images.githubusercontent.com/55052074/111630491-c2eeb480-8835-11eb-8865-c6de686f5176.jpg" width="30%"/> <img src="https://user-images.githubusercontent.com/55052074/111630516-caae5900-8835-11eb-828c-401e9718ad52.jpg" width="30%"/></p>

- 시, 분을 설정하면 수면 사이클을 기반으로 일어날 시각 또는 잠 들어야할 시각을 추천합니다.
- 상단의 Toggle Switch를 통해 일어날 시각을 계산할지 잠 들 시각을 계산할지 설정합니다.

## 기타 사항

- 잠 들 시간 전환 애니메이션 구현
<img src="https://user-images.githubusercontent.com/55052074/111630678-f3cee980-8835-11eb-86ea-b30f5b643e9f.gif" width="30%"/>
