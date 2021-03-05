
const intervalID = setInterval(() => {
	const root = document.querySelector('#root');
	if (root) {
		ReactDOM.render(
			<React.StrictMode>
				<App />
			</React.StrictMode>,
			root
		)
		clearInterval(intervalID);
	}
}, 100);

class App extends React.Component {
	constructor(props) {
		super(props);
		this.userModel = new Model('/users', true);
	}
	render() {
		return (
			<div>
				<Users title={'Users (system):'} model={this.userModel} />
				<br />
				<GitHubUsers title={'GitHub Users (most followed):'} model={this.userModel} />
				<br /><br />
				<b><a href="/swagger-ui.html" target="_blank" style={{ 'padding-left': '10pt', 'font-size': '100%' }}>Swagger Documentation</a></b>
				<br />
				<a href="/h2-dougp_db" target="_blank" style={{ 'padding-left': '10pt', 'font-size': '100%' }}>Database</a>
				<br /><br />
				<a href="/logout" target="_blank" style={{ 'padding-left': '10pt', 'font-size': '100%', 'font-weight': 'bold' }}>logout</a>
			</div>
		)
	}
}


const GitHubUsers = (props) => {
	const model = props.model;
	let [data, setData] = React.useState(model.gitHubUsers);
	model.gitHubListener = setData;

	const users = data.map((user) => {
		return <GitHubUser key={user.login} user={user} />
	})
	return (
		<React.Fragment>
			<h2>{props.title}</h2>
			<table>
				{users}
			</table>
		</React.Fragment>
	)
}

const GitHubUser = (props) => {
	const user = props.user;
	return (
		<React.Fragment>
			<tr>
				<td style={{ 'padding-left': '10pt' }}>
					{user.name}
				</td>
				<td style={{ 'padding-left': '10pt' }}>
					<a href={user.htmlUrl} target="_blank">{user.login}</a>
				</td>
			</tr>
		</React.Fragment>
	)
}

const Users = (props) => {
	const model = props.model;
	let [data, setData] = React.useState(model.data);
	model.listeners.push(setData);

	const users = data.map((user) => {
		return <User key={user.id} model={model} user={user} editing={model.editing(user)} />
	})
	return (
		<form onSubmit={e => { e.preventDefault() }}>
			<h2>{props.title}</h2>
			<table>
				{users}
			</table>
			<input type="text" name="newUser_name" size="30" placeholder="add new user NAME here" title="New User Name" maxlength="255" />
			<input type="text" name="newUser_login" size="15" placeholder="LOGIN here" title="New User Login" maxlength="60" />
			<input type="password" name="newUser_password" size="10" placeholder="password" title="New User Password" maxlength="60" />
			<input class="btn-primary" type="submit" value="add" onClick={(e) => {
				const form = e.target.form; model.save(
					{
						"seq": data.length,
						"login": form.newUser_login.value,
						"name": form.newUser_name.value,
						"password": form.newUser_password.value
					})
			}} title="Add User" />
		</form>
	)
}

const User = (props) => {
	const model = props.model;
	const editing = props.editing;
	let [user, setUser] = React.useState(props.user);

	const handleEvent = (e) => {
		e.preventDefault();
		const value = e.target.value;
		setUser({ ...user, ...{ [e.target.name]: value } });
	}
	const save = () => {
		model.save(user, true) //merge
			.then((object) => { setUser(object) })
			.catch((error) => { alert(error) });
	}
	const cancel = () => {
		setUser(user = model.cancel(user)); //undo
	}
	const edit = () => {
		model.edit(user)
	}
	const remove = () => {
		if (confirm('Delete?')) {
			model.remove(user)
		}
	}
	return (
		<React.Fragment>
			<tr>
				<td style={{ 'padding-left': '10pt' }}>
					{editing &&
						<input type="text" name="name" value={user.name} onChange={handleEvent} maxlength="60" />
					}
					{!editing &&
						<span>{user.name}</span>
					}
				</td>
				<td style={{ 'padding-left': '10pt' }}>
					{editing &&
						<input type="text" name="login" value={user.login} onChange={handleEvent} maxlength="60" />
					}
					{!editing &&
						<span>{user.login}</span>
					}
				</td>
				<td style={{ 'padding-left': '10pt' }}>
					{editing &&
						<input type="password" name="password" value={user.password} onChange={handleEvent} maxlength="60" />
					}
					{!editing &&
						<span>{user.password}</span>
					}
				</td>
				<td>
					&nbsp;&nbsp;
					{!editing &&
						<a href="javascript:void(0);" onClick={edit} title="Edit">
							<i class="glyphicon glyphicon-pencil"></i>
						</a>
					}
					{!editing &&
						<a href="javascript:void(0);" onClick={remove} title="Delete">
							&nbsp;&nbsp;<i class="glyphicon glyphicon-remove"></i>
						</a>
					}
					{editing &&
						<a href="javascript:void(0);" onClick={save} title="Save">
							<i class="glyphicon glyphicon-ok" style={{ 'color': 'green', 'font-size': '135%' }}></i>
						</a>
					}
					{editing &&
						<a href="javascript:void(0);" onClick={cancel} title="Cancel">
							&nbsp;&nbsp;<i class="glyphicon glyphicon-repeat" style={{ 'color': 'red' }}></i>
						</a>
					}
				</td>
			</tr>
		</React.Fragment>
	)
}

class Model {
	static headers = {
		'Content-Type': 'application/json'
	};
	static newId = -1;
	data = [];
	editList = [];
	listeners = [];
	user = {};
	constructor(endPoint, load) {
		this.endPoint = endPoint;
		if (load) {
			this.load();
			this.loadGitHubUsers();
		}
	}

	fireEvent = () => {
		this.data = [...this.data];
		for (const listener of this.listeners) {
			try {
				listener(this.data);
			} catch (e) {
				console.error('Model.fireEvent: ' + this.endPoint + ': ' + e);
			}
		}
	}

	load = async () => {
		try {
			let response = await fetch(this.endPoint, {
				method: 'GET',
				headers: Model.headers
			});
			this.data = await response.json();
			this.fireEvent();
			return this.data;
		} catch (e) {
			console.error('Model.load: ' + this.endPoint + ': ' + e);
			throw e;
		}
	}

	save = async (object, merge) => {
		const newObject = (!object.id || object.id < 0);
		if (!object.id) {
			object.id = Model.newId--;
		} else if (merge) {
			const obj = this.data.find(element => element.id == object.id);
			if (obj) {
				object = { ...obj, ...object };
			}
		}
		try {
			let response = await fetch(this.endPoint, {
				method: 'POST',
				headers: Model.headers,
				body: JSON.stringify(object)
			});
			const obj = await response.json();
			if (newObject) {
				this.data.push(obj);
			} else {
				this.data[this.data.findIndex(element => element.id == obj.id)] = obj;
				this.editList.splice(this.editList.indexOf(object.id), 1);
			}
			this.fireEvent();
			return obj;
		} catch (e) {
			console.error('Model.save: ' + this.endPoint + ': ' + e);
			throw e;
		}
	}

	remove = async (object) => {
		try {
			let code;
			if (object.id && object.id >= 0) {
				let response = await fetch(this.endPoint + '/' + object.id, {
					method: 'DELETE',
					headers: Model.headers,
				});
				code = await response.json();
			}
			const idx = this.data.findIndex(element => element.id == object.id);
			if (idx >= 0) {
				this.data.splice(idx, 1);
			}
			this.fireEvent();
			return code;
		} catch (e) {
			console.error('Model.remove: ' + this.endPoint + ': ' + e);
			throw e;
		}
	}

	edit = (object) => {
		this.editList.push(object.id);
		this.fireEvent();
	}

	editing = (object) => {
		return this.editList.includes(object.id);
	}

	cancel = (object) => {
		this.editList.splice(this.editList.indexOf(object.id), 1);
		this.fireEvent();
		return this.data.find(element => element.id == object.id);
	}

	gitHubUsers = [];
	gitHubListener = () => { };

	loadGitHubUsers = async () => {
		try {
			do {
				let response = await fetch(this.endPoint + '/gitHubUsers/' + this.gitHubUsers.length, {
					method: 'GET',
					headers: Model.headers
				});
				let text = await response.text();
				if (text != '') {
					let users = JSON.parse(text);
					this.gitHubUsers = [...this.gitHubUsers, ...users];
					this.gitHubListener(this.gitHubUsers);
				} else {
					break;
				}
			} while (this.gitHubUsers.length < 500);
		} catch (e) {
			console.error('Model.gitHubUsers: ' + this.gitHubUsers.length + ': ' + e);
			throw e;
		}
	}

}

